package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.RecruitmentEntity;
import com.edso.resume.api.domain.entities.UserEntity;
import com.edso.resume.api.domain.request.CreateRecruitmentRequest;
import com.edso.resume.api.domain.request.DeleteRecruitmentRequest;
import com.edso.resume.api.domain.request.UpdateRecruitmentRequest;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ThreadConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RecruitmentServiceImpl extends BaseService implements RecruitmentService, IDictionaryValidator {

    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    protected RecruitmentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<RecruitmentEntity> findAll(HeaderInfo info, Integer page, Integer size) {
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_RECRUITMENT, null, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<RecruitmentEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                RecruitmentEntity sourceCV = RecruitmentEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .title(AppUtils.parseString(doc.get(DbKeyConfig.TITLE)))
                        .jobId(AppUtils.parseString(doc.get(DbKeyConfig.JOB_ID)))
                        .jobName(AppUtils.parseString(doc.get(DbKeyConfig.JOB_NAME)))
                        .address(AppUtils.parseString(doc.get(DbKeyConfig.ADDRESS)))
                        .typeOfJob(AppUtils.parseString(doc.get(DbKeyConfig.TYPE_OF_JOB)))
                        .quantity(AppUtils.parseString(doc.get(DbKeyConfig.QUANTITY)))
                        .detailOfSalary(AppUtils.parseString(doc.get(DbKeyConfig.DETAIL_OF_SALARY)))
                        .jobDescription(AppUtils.parseString(doc.get(DbKeyConfig.JOB_DESCRIPTION)))
                        .requirementOfJob(AppUtils.parseString(doc.get(DbKeyConfig.REQUIREMENT_OF_JOB)))
                        .deadLine(AppUtils.parseLong(doc.get(DbKeyConfig.DEAD_LINE)))
                        .talentPoolId(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_ID)))
                        .talentPoolName(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_NAME)))
                        .interviewer((List<UserEntity>) doc.get(DbKeyConfig.INTERVIEWER))
                        .build();
                rows.add(sourceCV);
            }
        }
        GetArrayResponse<RecruitmentEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_RECRUITMENT, null));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createRecruitment(CreateRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();

        String idProfile = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        try {

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            if(request.getInterviewer() != null && !request.getInterviewer().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewer(), db, this));
            }
            int total = rs.size();

            for (DictionaryValidateProcessor p : rs) {
                Thread t = new Thread(p);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult result = queue.poll();
                if (result != null) {
                    if (result.getKey().equals(key)) {
                        if (!result.isResult()) {
                            response.setFailed((String) result.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(result);
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

            // conventions
            Document recruitment = new Document();
            recruitment.append(DbKeyConfig.ID, idProfile);
            recruitment.append(DbKeyConfig.TITLE, request.getTitle());
            recruitment.append(DbKeyConfig.JOB_ID, request.getJob());
            recruitment.append(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName());
            recruitment.append(DbKeyConfig.ADDRESS, request.getAddress());
            recruitment.append(DbKeyConfig.TYPE_OF_JOB, request.getTypeOfJob());
            recruitment.append(DbKeyConfig.QUANTITY, request.getQuantity());
            recruitment.append(DbKeyConfig.DETAIL_OF_SALARY, request.getDetailOfSalary());
            recruitment.append(DbKeyConfig.JOB_DESCRIPTION, request.getJobDescription());
            recruitment.append(DbKeyConfig.REQUIREMENT_OF_JOB, request.getRequirementOfJob());
            recruitment.append(DbKeyConfig.DEAD_LINE, request.getDeadLine());
            recruitment.append(DbKeyConfig.TALENT_POOL_ID, request.getTalentPool());
            recruitment.append(DbKeyConfig.TALENT_POOL_NAME, dictionaryNames.getTalentPoolName());
            recruitment.append(DbKeyConfig.INTERVIEWER, dictionaryNames.getInterviewer());
            recruitment.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            recruitment.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_RECRUITMENT, recruitment);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
    }

    @Override
    public BaseResponse updateRecruitment(UpdateRecruitmentRequest request) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            if(request.getInterviewer() != null && !request.getInterviewer().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewer(), db, this));
            }
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

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.ID, request.getId()),
                    Updates.set(DbKeyConfig.TITLE, request.getTitle()),
                    Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
                    Updates.set(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName()),
                    Updates.set(DbKeyConfig.ADDRESS, request.getAddress()),
                    Updates.set(DbKeyConfig.TYPE_OF_JOB, request.getTypeOfJob()),
                    Updates.set(DbKeyConfig.QUANTITY, request.getQuantity()),
                    Updates.set(DbKeyConfig.DETAIL_OF_SALARY, request.getDetailOfSalary()),
                    Updates.set(DbKeyConfig.JOB_DESCRIPTION, request.getJobDescription()),
                    Updates.set(DbKeyConfig.REQUIREMENT_OF_JOB, request.getRequirementOfJob()),
                    Updates.set(DbKeyConfig.DEAD_LINE, request.getDeadLine()),
                    Updates.set(DbKeyConfig.TALENT_POOL_ID, request.getTalentPool()),
                    Updates.set(DbKeyConfig.TALENT_POOL_NAME, dictionaryNames.getTalentPoolName()),
                    Updates.set(DbKeyConfig.INTERVIEWER, dictionaryNames.getInterviewer()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_RECRUITMENT, cond, updates, true);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
    }

    @Override
    public BaseResponse deleteRecruitment(DeleteRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_RECRUITMENT, cond);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        return new BaseResponse(0, "OK");
    }

    private DictionaryNamesEntity getDictionayNames(List<DictionaryValidateProcessor> rs) {
        DictionaryNamesEntity dictionaryNames = new DictionaryNamesEntity();
        for (DictionaryValidateProcessor r : rs) {
            switch (r.getResult().getType()) {
                case ThreadConfig.JOB: {
                    dictionaryNames.setJobName(AppUtils.parseString(r.getResult().getName()));
                    break;
                }
                case ThreadConfig.TALENT_POOL: {
                    dictionaryNames.setTalentPoolName(AppUtils.parseString(r.getResult().getName()));
                    break;
                }
                case ThreadConfig.LIST_USER: {
                    dictionaryNames.setInterviewer((List<Document>) r.getResult().getName());
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
