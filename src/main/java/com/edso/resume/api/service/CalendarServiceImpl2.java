package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity2;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.TimeEntity;
import com.edso.resume.api.domain.entities.UserEntity;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.rabbitmq.publish.PublishCandidateEmail;
import com.edso.resume.api.domain.rabbitmq.publish.PublishPresenter;
import com.edso.resume.api.domain.rabbitmq.publish.PublishRecruitmentCouncil;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

@Service
public class CalendarServiceImpl2 extends BaseService implements CalendarService2, IDictionaryValidator {

    private final HistoryService historyService;
    private final HistoryEmailService historyEmailService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();
    private final RabbitMQOnlineActions rabbitMQOnlineActions;

    @Value("${calendar.timeCheck}")
    private long timeCheck;

    @Value("${calendar.nLoop}")
    private int nLoop;

    public CalendarServiceImpl2(MongoDbOnlineSyncActions db, HistoryService historyService, HistoryEmailService historyEmailService, RabbitMQOnlineActions rabbitMQOnlineActions) {
        super(db);
        this.historyService = historyService;
        this.historyEmailService = historyEmailService;
        this.rabbitMQOnlineActions = rabbitMQOnlineActions;
    }

    @Override
    public GetArrayCalendarResponse<CalendarEntity2> findAllCalendar(HeaderInfo info, String idProfile, String key, String keySearch, String recruitment) {
        GetArrayCalendarResponse<CalendarEntity2> resp = new GetArrayCalendarResponse<>();
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(keySearch)) {
            c.add(Filters.or(Filters.regex(DbKeyConfig.FULL_NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(keySearch))), Filters.regex(DbKeyConfig.RECRUITMENT_NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(keySearch)))));
        }
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        }
        if (!Strings.isNullOrEmpty(recruitment)) {
            c.add(Filters.eq(DbKeyConfig.RECRUITMENT_ID, recruitment));
        }
        if (!Strings.isNullOrEmpty(key)) {
            if (key.equals("create")) {
                c.add(Filters.eq(DbKeyConfig.CREATE_BY, info.getUsername()));
            }
            if (key.equals("join")) {
                c.add(Filters.eq(DbKeyConfig.JOIN_USERNAME, info.getUsername()));
            }
        }
        Bson sort = Filters.eq(DbKeyConfig.DATE, 1);
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, sort, 0, 0);
        List<CalendarEntity2> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CalendarEntity2 calendar = CalendarEntity2.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .recruitmentId(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_ID)))
                        .recruitmentName(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_NAME)))
                        .date(AppUtils.parseLong(doc.get(DbKeyConfig.DATE)))
                        .interviewTime(AppUtils.parseLong(doc.get(DbKeyConfig.INTERVIEW_TIME)))
                        .interviewAddressId(AppUtils.parseString(doc.get(DbKeyConfig.INTERVIEW_ADDRESS_ID)))
                        .interviewAddressName(AppUtils.parseString(doc.get(DbKeyConfig.INTERVIEW_ADDRESS_NAME)))
                        .floor(AppUtils.parseString(doc.get(DbKeyConfig.FLOOR)))
                        .type(AppUtils.parseString(doc.get(DbKeyConfig.TYPE)))
                        .interviewers((List<UserEntity>) doc.get(DbKeyConfig.INTERVIEWERS))
                        .note(AppUtils.parseString(doc.get(DbKeyConfig.NOTE)))
                        .avatarColor(AppUtils.parseString(doc.get(DbKeyConfig.AVATAR_COLOR)))
                        .createAt(AppUtils.parseLong(doc.get(DbKeyConfig.CREATE_AT)))
                        .createBy(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_BY)))
                        .build();
                calendars.add(calendar);
            }
        }
        resp.setSuccess();
        resp.setCalendars(calendars);
        return resp;
    }

    @Override
    public BaseResponse createCalendarProfile(CreateCalendarProfileRequest2 request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate) {

        BaseResponse response = new BaseResponse();

        String idProfile = request.getIdProfile();
        String key = UUID.randomUUID().toString();

        try {

            //Validate
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.CALENDAR_PROFILE, idProfile, db, this));
            if (request.getInterviewers() != null && !request.getInterviewers().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewers(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getRecruitmentId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.ADDRESS, request.getInterviewAddress(), db, this));
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
            calendar.append(DbKeyConfig.DATE, request.getDate());
            calendar.append(DbKeyConfig.INTERVIEW_TIME, request.getInterviewTime());
            calendar.append(DbKeyConfig.INTERVIEW_ADDRESS_ID, request.getInterviewAddress());
            calendar.append(DbKeyConfig.INTERVIEW_ADDRESS_NAME, dictionaryNames.getAddressName());
            calendar.append(DbKeyConfig.FLOOR, request.getFloor());
            calendar.append(DbKeyConfig.TYPE, request.getType());
            calendar.append(DbKeyConfig.INTERVIEWERS, dictionaryNames.getInterviewer());
            calendar.append(DbKeyConfig.NOTE, request.getNote());
            calendar.append(DbKeyConfig.AVATAR_COLOR, request.getAvatarColor());
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

            //publish rabbit
            if (!Strings.isNullOrEmpty(candidate.getSubjectCandidate()) && !Strings.isNullOrEmpty(candidate.getContentCandidate())) {
                new Thread(new PublishCandidateEmail(rabbitMQOnlineActions, historyEmailService, request.getInfo(), candidate, idProfile, TypeConfig.CALENDAR_CANDIDATE)).start();
            }
            if (!Strings.isNullOrEmpty(presenter.getSubjectPresenter()) && !Strings.isNullOrEmpty(presenter.getContentPresenter())) {
                new Thread(new PublishPresenter(rabbitMQOnlineActions, historyEmailService, request.getInfo(), presenter, idProfile, TypeConfig.CALENDAR_PRESENTER)).start();
            }
            if (!Strings.isNullOrEmpty(recruitmentCouncil.getSubjectRecruitmentCouncil()) && !Strings.isNullOrEmpty(recruitmentCouncil.getContentRecruitmentCouncil())) {
                new Thread(new PublishRecruitmentCouncil(rabbitMQOnlineActions, historyEmailService, request.getInfo(), recruitmentCouncil, id, idProfile)).start();
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

    @Override
    public BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest2 request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.CALENDAR, id, db, this));
            if (request.getInterviewers() != null && !request.getInterviewers().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewers(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getRecruitmentId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.ADDRESS, request.getInterviewAddress(), db, this));
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
                    Updates.set(DbKeyConfig.RECRUITMENT_ID, request.getRecruitmentId()),
                    Updates.set(DbKeyConfig.RECRUITMENT_NAME, dictionaryNames.getRecruitmentName()),
                    Updates.set(DbKeyConfig.DATE, request.getDate()),
                    Updates.set(DbKeyConfig.INTERVIEW_TIME, request.getInterviewTime()),
                    Updates.set(DbKeyConfig.INTERVIEW_ADDRESS_ID, request.getInterviewAddress()),
                    Updates.set(DbKeyConfig.INTERVIEW_ADDRESS_NAME, dictionaryNames.getAddressName()),
                    Updates.set(DbKeyConfig.FLOOR, request.getFloor()),
                    Updates.set(DbKeyConfig.TYPE, request.getType()),
                    Updates.set(DbKeyConfig.INTERVIEWERS, dictionaryNames.getInterviewer()),
                    Updates.set(DbKeyConfig.NOTE, request.getNote()),
                    Updates.set(DbKeyConfig.RECRUITMENT_NAME_SEARCH, AppUtils.parseVietnameseToEnglish(dictionaryNames.getRecruitmentName())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(dictionaryNames.getIdProfile(), TypeConfig.UPDATE, "Sửa lịch phỏng vấn", request.getInfo());

            //publish rabbit
            if (!Strings.isNullOrEmpty(candidate.getSubjectCandidate()) && !Strings.isNullOrEmpty(candidate.getContentCandidate())) {
                new Thread(new PublishCandidateEmail(rabbitMQOnlineActions, historyEmailService, request.getInfo(), candidate, dictionaryNames.getIdProfile(), TypeConfig.CALENDAR_CANDIDATE)).start();
            }
            if (!Strings.isNullOrEmpty(presenter.getSubjectPresenter()) && !Strings.isNullOrEmpty(presenter.getContentPresenter())) {
                new Thread(new PublishPresenter(rabbitMQOnlineActions, historyEmailService, request.getInfo(), presenter, dictionaryNames.getIdProfile(), TypeConfig.CALENDAR_PRESENTER)).start();
            }
            if (!Strings.isNullOrEmpty(recruitmentCouncil.getSubjectRecruitmentCouncil()) && !Strings.isNullOrEmpty(recruitmentCouncil.getContentRecruitmentCouncil())) {
                new Thread(new PublishRecruitmentCouncil(rabbitMQOnlineActions, historyEmailService, request.getInfo(), recruitmentCouncil, id, dictionaryNames.getIdProfile())).start();
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

    @Override
    public BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);
            if (idDocument == null) {
                response.setFailed("Không tồn tại id calendar này");
                return response;
            }

            Bson update = Updates.combine(
                    Updates.set(DbKeyConfig.CALENDAR, null)
            );
            db.update(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idDocument.get(DbKeyConfig.ID_PROFILE)), update, true);

            db.delete(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

            //Insert history to DB
            historyService.createHistory(AppUtils.parseString(idDocument.get(DbKeyConfig.ID_PROFILE)), TypeConfig.DELETE, "Xóa lịch phỏng vấn", request.getInfo());
            response.setSuccess();
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        return response;
    }

    @Override
    public void deleteCalendarByIdProfile(String idProfile) {
        Bson cond = Filters.eq(DbKeyConfig.ID_PROFILE, idProfile);
        db.delete(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);
        logger.info("deleteCalendarByIdProfile idProfile: {}", idProfile);
    }

    public void alarmInterview() {
        Bson c = Filters.eq(DbKeyConfig.CHECK, "0");
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, c, null, 0, 0);
        List<TimeEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                TimeEntity calendar = TimeEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .check(AppUtils.parseString(doc.get(DbKeyConfig.CHECK)))
                        .nLoop(AppUtils.parseInt(doc.get(DbKeyConfig.N_LOOP)))
                        .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                        .build();
                calendars.add(calendar);
            }
        }

        for (TimeEntity calendar : calendars) {
            long differenceTime = calendar.getTime() - System.currentTimeMillis();
            int n = calendar.getNLoop();
            if (differenceTime <= timeCheck) {
                Bson con = Filters.eq(DbKeyConfig.ID, calendar.getId());
                if (n < nLoop) {
                    n++;
                    Bson updates = Updates.combine(
                            Updates.set(DbKeyConfig.N_LOOP, n)
                    );
                    db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, con, updates, true);
                    try {
                        Date date = new Date(calendar.getTime());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");
                        String dateTime = dateFormat.format(date);
                        rabbitMQOnlineActions.publishEmailToRabbit(calendar.getEmail(), dateTime);
                    } catch (Throwable e) {
                        logger.error("Exception: ", e);
                    }
                } else {
                    Bson updates = Updates.combine(
                            Updates.set(DbKeyConfig.CHECK, "1")
                    );
                    db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, con, updates, true);
                }

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
                    dictionaryNames.setEmailUser(r.getResult().getMailRef());
                    dictionaryNames.setEmail((String) r.getResult().getName());
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
