package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.EventEntity;
import com.edso.resume.api.domain.entities.ProfileRabbitMQEntity;
import com.edso.resume.api.domain.request.ChangeRecruitmentRequest;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class ChangeRecruitmentServiceImpl extends BaseService implements ChangeRecruitmentService, IDictionaryValidator {

    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();
    private final RabbitTemplate rabbitTemplate;
    private final HistoryService historyService;

    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;

    protected ChangeRecruitmentServiceImpl(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate, HistoryService historyService) {
        super(db);
        this.rabbitTemplate = rabbitTemplate;
        this.historyService = historyService;
    }

    private void publishActionToRabbitMQ(Object profile) {
        EventEntity event = new EventEntity(RabbitMQConfig.UPDATE_RECRUITMENT, profile);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
        logger.info("=>publishActionToRabbitMQ type: {}, profile: {}", RabbitMQConfig.UPDATE_RECRUITMENT, profile);
    }

    @Override
    public BaseResponse changeRecruitment(ChangeRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();
        try {
            //Validate
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.CHANGE_RECRUITMENT_PROFILE, request.getIdProfile(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getRecruitmentId(), db, this));
            DictionaryValidateProcessor dictionaryValidateProcessor = new DictionaryValidateProcessor(key, ThreadConfig.STATUS_CV, request.getStatusCVId(), db, this);
            dictionaryValidateProcessor.setRecruitmentId(request.getRecruitmentId());
            rs.add(dictionaryValidateProcessor);
            int total = rs.size();

            for (DictionaryValidateProcessor r : rs) {
                Thread t = new Thread(r);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult validatorResult = queue.poll();
                if (validatorResult != null) {
                    if (validatorResult.getKey().equals(key)) {
                        if (!validatorResult.isResult()) {
                            response.setFailed((String) validatorResult.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(validatorResult);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);
            Document document = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, dictionaryNames.getCreateRecruitmentBy()));

            Bson update = Updates.combine(
                    Updates.set(DbKeyConfig.RECRUITMENT_ID, request.getRecruitmentId()),
                    Updates.set(DbKeyConfig.RECRUITMENT_NAME, dictionaryNames.getRecruitmentName()),
                    Updates.set(DbKeyConfig.RECRUITMENT_TIME, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.STATUS_CV_ID, request.getStatusCVId()),
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName()),
                    Updates.set(DbKeyConfig.CREATE_RECRUITMENT_BY, dictionaryNames.getCreateRecruitmentBy()),
                    Updates.set(DbKeyConfig.FULL_NAME_CREATOR, AppUtils.parseString(document.get(DbKeyConfig.FULL_NAME))),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );

            db.update(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, request.getIdProfile()), update, true);

            Document recruitment1 = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.ID, dictionaryNames.getRecruitmentId()));
            if (recruitment1 != null) {
                List<Document> doc1 = (List<Document>) recruitment1.get(DbKeyConfig.INTERVIEW_PROCESS);

                for (Document document1 : doc1) {
                    if (AppUtils.parseString(document1.get(DbKeyConfig.ID)).equals(dictionaryNames.getStatusCVId())) {
                        Bson cond = Filters.and(Filters.eq(DbKeyConfig.ID, dictionaryNames.getRecruitmentId()), Filters.eq("interview_process.id", dictionaryNames.getStatusCVId()));
                        Bson updateTotal = Updates.combine(
                                Updates.set("interview_process.$.total", AppUtils.parseLong(document1.get(DbKeyConfig.TOTAL)) - 1)
                        );
                        db.update(CollectionNameDefs.COLL_RECRUITMENT, cond, updateTotal, true);
                        break;
                    }
                }
            }

            Document recruitment = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.ID, request.getRecruitmentId()));
            if (recruitment != null) {
                List<Document> doc = (List<Document>) recruitment.get(DbKeyConfig.INTERVIEW_PROCESS);

                for (Document document1 : doc) {
                    if (AppUtils.parseString(document1.get(DbKeyConfig.ID)).equals(request.getStatusCVId())) {
                        Bson cond1 = Filters.and(Filters.eq(DbKeyConfig.ID, request.getRecruitmentId()), Filters.eq("interview_process.id", request.getStatusCVId()));
                        Bson updateTotal = Updates.combine(
                                Updates.set("interview_process.$.total", AppUtils.parseLong(document1.get(DbKeyConfig.TOTAL)) + 1)
                        );
                        db.update(CollectionNameDefs.COLL_RECRUITMENT, cond1, updateTotal, true);
                        break;
                    }
                }
            }
            //Insert history to DB
            historyService.createHistory(request.getIdProfile(), TypeConfig.UPDATE, "Chuyển ứng viên sang tin tuyển dụng khác", request.getInfo());

            ProfileRabbitMQEntity profileRabbitMQ = new ProfileRabbitMQEntity();
            profileRabbitMQ.setRecruitmentId(request.getRecruitmentId());
            profileRabbitMQ.setRecruitmentName(dictionaryNames.getRecruitmentName());
            profileRabbitMQ.setStatusCVId(request.getStatusCVId());
            profileRabbitMQ.setStatusCVName(dictionaryNames.getStatusCVName());

            publishActionToRabbitMQ(profileRabbitMQ);

        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
        response.setSuccess();
        return response;
    }

    private DictionaryNamesEntity getDictionayNames(List<DictionaryValidateProcessor> rs) {
        DictionaryNamesEntity dictionaryNames = new DictionaryNamesEntity();
        for (DictionaryValidateProcessor r : rs) {
            switch (r.getResult().getType()) {
                case ThreadConfig.CHANGE_RECRUITMENT_PROFILE: {
                    dictionaryNames.setRecruitmentId((String) r.getResult().getName());
                    dictionaryNames.setStatusCVId(r.getResult().getStatusCVId());
                    break;
                }
                case ThreadConfig.RECRUITMENT: {
                    dictionaryNames.setRecruitmentName((String) r.getResult().getName());
                    dictionaryNames.setCreateRecruitmentBy(r.getResult().getFullName());
                    break;
                }
                case ThreadConfig.STATUS_CV: {
                    dictionaryNames.setStatusCVName((String) r.getResult().getName());
                    break;
                }
                default: {
                    logger.info("Không có tên của dictionary này!");
                    break;
                }
            }
        }
        return dictionaryNames;
    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}
