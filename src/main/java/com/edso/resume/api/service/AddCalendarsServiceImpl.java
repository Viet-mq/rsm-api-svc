package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.api.domain.json.JsonHelper;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AddCalendarsServiceImpl extends BaseService implements AddCalendarsService, IDictionaryValidator {

    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();
    private final HistoryService historyService;
    private final RabbitMQOnlineActions rabbitMQOnlineActions;
    private final HistoryEmailService historyEmailService;

    protected AddCalendarsServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitMQOnlineActions rabbitMQOnlineActions, HistoryEmailService historyEmailService) {
        super(db);
        this.historyService = historyService;
        this.rabbitMQOnlineActions = rabbitMQOnlineActions;
        this.historyEmailService = historyEmailService;
    }

    @Override
    public BaseResponse addCalendars(AddCalendarsRequest request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate) {
        String key = UUID.randomUUID().toString();
        BaseResponse response = null;
        try {
            List<CreateTimeCalendarRequest> list = JsonHelper.convertJsonToList(request.getTimes(), CreateTimeCalendarRequest[].class);
            if (list == null || list.isEmpty()) {
                return new BaseResponse(-1, "Vui lòng nhập 1 list ứng viên");
            }
            for (CreateTimeCalendarRequest calendarRequest : list) {
                response = calendarRequest.validate();
                if (response != null) {
                    return response;
                }
            }
            response = new BaseResponse();
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            if (request.getInterviewers() != null && !request.getInterviewers().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewers(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getRecruitmentId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.ADDRESS, request.getInterviewAddress(), db, this));

            List<IdEntity> ids = new ArrayList<>();
            for (CreateTimeCalendarRequest request2 : list) {
                String idProfile = request2.getIdProfile();
                //Validate
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.CALENDAR_PROFILE, idProfile, db, this));
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

                String id = UUID.randomUUID().toString();
                Document calendar = new Document();
                calendar.append(DbKeyConfig.ID, id);
                calendar.append(DbKeyConfig.ID_PROFILE, idProfile);
                calendar.append(DbKeyConfig.FULL_NAME, dictionaryNames.getFullName());
                calendar.append(DbKeyConfig.RECRUITMENT_ID, request.getRecruitmentId());
                calendar.append(DbKeyConfig.RECRUITMENT_NAME, dictionaryNames.getRecruitmentName());
                calendar.append(DbKeyConfig.DATE, request2.getDate());
                calendar.append(DbKeyConfig.INTERVIEW_TIME, request2.getInterviewTime());
                calendar.append(DbKeyConfig.INTERVIEW_ADDRESS_ID, request.getInterviewAddress());
                calendar.append(DbKeyConfig.INTERVIEW_ADDRESS_NAME, dictionaryNames.getAddressName());
                calendar.append(DbKeyConfig.FLOOR, request.getFloor());
                calendar.append(DbKeyConfig.TYPE, request.getType());
                calendar.append(DbKeyConfig.INTERVIEWERS, dictionaryNames.getInterviewer());
                calendar.append(DbKeyConfig.NOTE, request.getNote());
                calendar.append(DbKeyConfig.AVATAR_COLOR, request2.getAvatarColor());
                calendar.append(DbKeyConfig.FULL_NAME_SEARCH, AppUtils.parseVietnameseToEnglish(dictionaryNames.getFullName()));
                calendar.append(DbKeyConfig.RECRUITMENT_NAME_SEARCH, AppUtils.parseVietnameseToEnglish(dictionaryNames.getRecruitmentName()));
                calendar.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
                calendar.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
                calendar.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
                calendar.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

                // insert to database
                db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, calendar);

                //Insert history to DB
                historyService.createHistory(idProfile, TypeConfig.CREATE, "Tạo lịch phỏng vấn", request.getInfo());

                if (!Strings.isNullOrEmpty(presenter.getSubjectPresenter()) && !Strings.isNullOrEmpty(presenter.getContentPresenter()) || !Strings.isNullOrEmpty(recruitmentCouncil.getSubjectRecruitmentCouncil()) && !Strings.isNullOrEmpty(recruitmentCouncil.getContentRecruitmentCouncil()) || !Strings.isNullOrEmpty(candidate.getSubjectCandidate()) && !Strings.isNullOrEmpty(candidate.getContentCandidate())) {
                    IdEntity idEntity = IdEntity.builder()
                            .calendarId(id)
                            .profileId(idProfile)
                            .build();
                    ids.add(idEntity);
                }
            }

            //publish rabbit
            if (!Strings.isNullOrEmpty(candidate.getSubjectCandidate()) && !Strings.isNullOrEmpty(candidate.getContentCandidate())) {
                List<String> paths = historyEmailService.createHistoryEmails(ids, candidate.getSubjectCandidate(), candidate.getContentCandidate(), candidate.getFileCandidates(), request.getInfo());
                rabbitMQOnlineActions.publishCandidateEmails(TypeConfig.CALENDARS_CANDIDATE, candidate, paths, ids);
            }
            if (!Strings.isNullOrEmpty(presenter.getSubjectPresenter()) && !Strings.isNullOrEmpty(presenter.getContentPresenter())) {
                List<String> paths = historyEmailService.createHistoryEmails(ids, presenter.getSubjectPresenter(), presenter.getContentPresenter(), presenter.getFilePresenters(), request.getInfo());
                rabbitMQOnlineActions.publishPresenterEmails(TypeConfig.CALENDARS_PRESENTER, presenter, paths, ids);
            }
            if (!Strings.isNullOrEmpty(recruitmentCouncil.getSubjectRecruitmentCouncil()) && !Strings.isNullOrEmpty(recruitmentCouncil.getContentRecruitmentCouncil())) {
                List<String> paths = historyEmailService.createHistoryEmails(ids, recruitmentCouncil.getSubjectRecruitmentCouncil(), recruitmentCouncil.getContentRecruitmentCouncil(), recruitmentCouncil.getFileRecruitmentCouncils(), request.getInfo());
                rabbitMQOnlineActions.publishRecruitmentCouncilEmails(TypeConfig.CALENDARS_INTERVIEWER, recruitmentCouncil, paths, ids);
            }

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

    private DictionaryNamesEntity getDictionayNames(List<DictionaryValidateProcessor> rs) {
        DictionaryNamesEntity dictionaryNames = new DictionaryNamesEntity();
        for (DictionaryValidateProcessor r : rs) {
            switch (r.getResult().getType()) {
                case ThreadConfig.CALENDAR: {
                    dictionaryNames.setIdProfile(AppUtils.parseString(r.getResult().getIdProfile()));
                    break;
                }
                case ThreadConfig.LIST_USER: {
                    dictionaryNames.setInterviewer((List<Document>) r.getResult().getName());
                    break;
                }
                case ThreadConfig.RECRUITMENT: {
                    dictionaryNames.setRecruitmentName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.ADDRESS: {
                    dictionaryNames.setAddressName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.CALENDAR_PROFILE: {
                    dictionaryNames.setFullName(r.getResult().getFullName());
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
