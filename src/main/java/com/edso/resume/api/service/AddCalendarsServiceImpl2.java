package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.api.domain.json.JsonHelper;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AddCalendarsServiceImpl2 extends AddCalendarsServiceImpl {

    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    @Value("${mail.fileSize}")
    private Long fileSize;

    public AddCalendarsServiceImpl2(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitMQOnlineActions rabbitMQOnlineActions, HistoryEmailService historyEmailService) {
        super(db, historyService, rabbitMQOnlineActions, historyEmailService);
    }

    @Override
    public BaseResponse addCalendars(AddCalendarsRequest request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate, RelatedPeopleRequest relatedPeople) {
        String key = UUID.randomUUID().toString();
        BaseResponse response = null;
        try {
            List<CreateTimeCalendarRequest> list = JsonHelper.convertJsonToList(request.getTimes(), CreateTimeCalendarRequest[].class);
            if (list.isEmpty()) {
                return new BaseResponse(-1, "Vui lòng nhập 1 list ứng viên");
            }
            if (presenter.getFilePresenters() != null && !presenter.getFilePresenters().isEmpty()) {
                for (MultipartFile file : presenter.getFilePresenters()) {
                    if (file != null && file.getSize() > fileSize) {
                        return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
                    }
                }
            }
            if (candidate.getFileCandidates() != null && !candidate.getFileCandidates().isEmpty()) {
                for (MultipartFile file : candidate.getFileCandidates()) {
                    if (file != null && file.getSize() > fileSize) {
                        return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
                    }
                }
            }
            if (recruitmentCouncil.getFileRecruitmentCouncils() != null && !recruitmentCouncil.getFileRecruitmentCouncils().isEmpty()) {
                for (MultipartFile file : recruitmentCouncil.getFileRecruitmentCouncils()) {
                    if (file != null && file.getSize() > fileSize) {
                        return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
                    }
                }
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
                calendar.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
                calendar.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations());

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

            if (!Strings.isNullOrEmpty(candidate.getSubjectCandidate()) && !Strings.isNullOrEmpty(candidate.getContentCandidate())) {
                historyEmailService.createHistoryEmails(TypeConfig.CALENDARS_CANDIDATE, ids, null, candidate.getSubjectCandidate(), candidate.getContentCandidate(), candidate.getFileCandidates(), request.getInfo());
            }
            if (!Strings.isNullOrEmpty(presenter.getSubjectPresenter()) && !Strings.isNullOrEmpty(presenter.getContentPresenter())) {
                historyEmailService.createHistoryEmails(TypeConfig.CALENDARS_PRESENTER, ids, presenter.getEmailPresenter(), presenter.getSubjectPresenter(), presenter.getContentPresenter(), presenter.getFilePresenters(), request.getInfo());
            }
            if (!Strings.isNullOrEmpty(recruitmentCouncil.getSubjectRecruitmentCouncil()) && !Strings.isNullOrEmpty(recruitmentCouncil.getContentRecruitmentCouncil())) {
                historyEmailService.createHistoryEmails(TypeConfig.CALENDARS_INTERVIEWER, ids, recruitmentCouncil.getEmailRecruitmentCouncil(), recruitmentCouncil.getSubjectRecruitmentCouncil(), recruitmentCouncil.getContentRecruitmentCouncil(), recruitmentCouncil.getFileRecruitmentCouncils(), request.getInfo());
            }
            if (!Strings.isNullOrEmpty(relatedPeople.getSubjectRelatedPeople()) && !Strings.isNullOrEmpty(relatedPeople.getContentRelatedPeople())) {
                historyEmailService.createHistoryEmails(TypeConfig.CALENDARS_RELATED_PEOPLE, ids, null, relatedPeople.getSubjectRelatedPeople(), relatedPeople.getContentRelatedPeople(), relatedPeople.getFileRelatedPeoples(), request.getInfo());
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
