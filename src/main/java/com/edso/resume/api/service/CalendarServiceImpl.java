package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.TimeEntity;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest;
import com.edso.resume.api.domain.request.DeleteCalendarProfileRequest;
import com.edso.resume.api.domain.request.UpdateCalendarProfileRequest;
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

@Service
public class CalendarServiceImpl extends BaseService implements CalendarService, IDictionaryValidator {
    private final HistoryService historyService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();
    private final RabbitMQOnlineActions rabbitMQOnlineActions;

    @Value("${calendar.timeCheck}")
    private long timeCheck;

    @Value("${calendar.nLoop}")
    private int nLoop;

    public CalendarServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitMQOnlineActions rabbitMQOnlineActions) {
        super(db);
        this.historyService = historyService;
        this.rabbitMQOnlineActions = rabbitMQOnlineActions;
    }

    @Override
    public GetArrayCalendarResponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile) {
        GetArrayCalendarResponse<CalendarEntity> resp = new GetArrayCalendarResponse<>();
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idProfile));
        if (idProfileDocument == null) {
            resp.setResult(ErrorCodeDefs.ID, "Id profile không tồn tại");
            return resp;
        }
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, sort, 0, 0);
        List<CalendarEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CalendarEntity calendar = CalendarEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .address(AppUtils.parseString(doc.get(DbKeyConfig.ADDRESS)))
                        .form(AppUtils.parseString(doc.get(DbKeyConfig.FORM)))
                        .interviewer((List<String>) doc.get(DbKeyConfig.INTERVIEWER))
                        .interviewee(AppUtils.parseString(doc.get(DbKeyConfig.INTERVIEWEE)))
                        .content(AppUtils.parseString(doc.get(DbKeyConfig.CONTENT)))
                        .question(AppUtils.parseString(doc.get(DbKeyConfig.QUESTION)))
                        .comments(AppUtils.parseString(doc.get(DbKeyConfig.COMMENTS)))
                        .evaluation(AppUtils.parseString(doc.get(DbKeyConfig.EVALUATION)))
                        .statusId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)))
                        .statusName(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)))
                        .reason(AppUtils.parseString(doc.get(DbKeyConfig.REASON)))
                        .timeStart(AppUtils.parseLong(doc.get(DbKeyConfig.TIME_START)))
                        .timeFinish(AppUtils.parseLong(doc.get(DbKeyConfig.TIME_FINISH)))
                        .build();
                calendars.add(calendar);
            }
        }
        resp.setSuccess();
        resp.setCalendars(calendars);
        return resp;
    }

    @Override
    public BaseResponse createCalendarProfile(CreateCalendarProfileRequest request) {

        BaseResponse response = new BaseResponse();

        String idProfile = request.getIdProfile();
        String key = UUID.randomUUID().toString();

        try {

            //Validate
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.STATUS_CV, request.getStatus(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewer(), db, this));
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

            String checkTime = "0";
            if (request.getTime() < System.currentTimeMillis()) {
                checkTime = "1";
            }
            String id = UUID.randomUUID().toString();
            Document calendar = new Document();
            calendar.append(DbKeyConfig.ID, id);
            calendar.append(DbKeyConfig.ID_PROFILE, idProfile);
            calendar.append(DbKeyConfig.EMAIL, dictionaryNames.getEmail());
            calendar.append(DbKeyConfig.TIME, request.getTime());
            calendar.append(DbKeyConfig.ADDRESS, request.getAddress());
            calendar.append(DbKeyConfig.FORM, request.getForm());
            calendar.append(DbKeyConfig.INTERVIEWER, request.getInterviewer());
            calendar.append(DbKeyConfig.INTERVIEWEE, request.getInterviewee());
            calendar.append(DbKeyConfig.CONTENT, request.getContent());
            calendar.append(DbKeyConfig.QUESTION, request.getQuestion());
            calendar.append(DbKeyConfig.COMMENTS, request.getComments());
            calendar.append(DbKeyConfig.EVALUATION, request.getEvaluation());
            calendar.append(DbKeyConfig.STATUS_CV_ID, request.getStatus());
            calendar.append(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName());
            calendar.append(DbKeyConfig.REASON, request.getReason());
            calendar.append(DbKeyConfig.TIME_START, request.getTimeStart());
            calendar.append(DbKeyConfig.TIME_FINISH, request.getTimeFinish());
            calendar.append(DbKeyConfig.CHECK, checkTime);
            calendar.append(DbKeyConfig.N_LOOP, 0);
            calendar.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            calendar.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            calendar.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            calendar.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, calendar);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.CREATE, "Tạo lịch phỏng vấn", request.getInfo());

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
    public BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        String key = UUID.randomUUID().toString();

        try {

            //Validate
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.CALENDAR, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.STATUS_CV, request.getStatus(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_USER, request.getInterviewer(), db, this));
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

            String check = "0";
            if (request.getTime() < System.currentTimeMillis()) {
                check = "1";
            }
            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.TIME, request.getTime()),
                    Updates.set(DbKeyConfig.ADDRESS, request.getAddress()),
                    Updates.set(DbKeyConfig.FORM, request.getForm()),
                    Updates.set(DbKeyConfig.INTERVIEWER, request.getInterviewer()),
                    Updates.set(DbKeyConfig.INTERVIEWEE, request.getInterviewee()),
                    Updates.set(DbKeyConfig.CONTENT, request.getContent()),
                    Updates.set(DbKeyConfig.QUESTION, request.getQuestion()),
                    Updates.set(DbKeyConfig.COMMENTS, request.getComments()),
                    Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                    Updates.set(DbKeyConfig.STATUS_CV_ID, request.getStatus()),
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName()),
                    Updates.set(DbKeyConfig.REASON, request.getReason()),
                    Updates.set(DbKeyConfig.CHECK, check),
                    Updates.set(DbKeyConfig.TIME_START, request.getTimeStart()),
                    Updates.set(DbKeyConfig.TIME_FINISH, request.getTimeFinish()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, updates, true);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(dictionaryNames.getIdProfile(), TypeConfig.UPDATE, "Sửa lịch phỏng vấn", request.getInfo());

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
                case ThreadConfig.STATUS_CV: {
                    dictionaryNames.setStatusCVName(AppUtils.parseString(r.getResult().getName()));
                    break;
                }
                case ThreadConfig.LIST_USER: {
                    dictionaryNames.setInterviewer((List<Document>) r.getResult().getName());
                    break;
                }
                case ThreadConfig.PROFILE: {
                    dictionaryNames.setEmail(AppUtils.parseString(r.getResult().getName()));
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
