package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.TimeEntity;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest;
import com.edso.resume.api.domain.request.DeleteCalendarProfileRequest;
import com.edso.resume.api.domain.request.UpdateCalendarProfileRequest;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarReponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
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
import java.util.regex.Pattern;

@Service
public class CalendarServiceImpl extends BaseService implements CalendarService, IDictionaryValidator {

    private final HistoryService historyService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    @Value("${calendar.timeCheck}")
    private long timeCheck;

    @Value("${calendar.nLoop}")
    private int nLoop;

    public CalendarServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService) {
        super(db);
        this.historyService = historyService;
    }

    @Override
    public GetArrayCalendarReponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile) {
        GetArrayCalendarReponse<CalendarEntity> resp = new GetArrayCalendarReponse<>();
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idProfile));
        if (idProfileDocument == null) {
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        }
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, null, 0, 0);
        List<CalendarEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CalendarEntity calendar = CalendarEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .address(AppUtils.parseString(doc.get(DbKeyConfig.ADDRESS)))
                        .form(AppUtils.parseString(doc.get(DbKeyConfig.FORM)))
                        .interviewer(parseList(doc.get(DbKeyConfig.INTERVIEWER)))
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
                            response.setFailed(validatorResult.getName());
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

            String statusCVName = null;

            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.STATUS_CV)) {
                    statusCVName = r.getResult().getName();
                }
            }

            Document calendar = new Document();
            calendar.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            calendar.append(DbKeyConfig.ID_PROFILE, idProfile);
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
            calendar.append(DbKeyConfig.STATUS_CV_NAME, statusCVName);
            calendar.append(DbKeyConfig.REASON, request.getReason());
            calendar.append(DbKeyConfig.TIME_START, request.getTimeStart());
            calendar.append(DbKeyConfig.TIME_FINISH, request.getTimeFinish());
            calendar.append(DbKeyConfig.CHECK, "0");
            calendar.append(DbKeyConfig.N_LOOP, 0);
            calendar.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            calendar.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            calendar.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            calendar.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, calendar);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.CREATE, "Tạo lịch phỏng vấn", request.getInfo().getUsername());

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
        String idProfile = request.getIdProfile();
        String key = UUID.randomUUID().toString();

        try {

            //Validate
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.CALENDAR, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.STATUS_CV, request.getStatus(), db, this));
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
                            response.setFailed(validatorResult.getName());
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

            String statusCVName = null;

            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.STATUS_CV)) {
                    statusCVName = r.getResult().getName();
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.ID_PROFILE, idProfile),
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
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, statusCVName),
                    Updates.set(DbKeyConfig.REASON, request.getReason()),
                    Updates.set(DbKeyConfig.TIME_START, request.getTimeStart()),
                    Updates.set(DbKeyConfig.TIME_FINISH, request.getTimeFinish()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, updates, true);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Sửa lịch phỏng vấn", request.getInfo().getUsername());

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
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        //Insert history to DB
        historyService.createHistory(request.getIdProfile(), TypeConfig.DELETE, "Xóa lịch phỏng vấn", request.getInfo().getUsername());
        response.setSuccess();

        return response;
    }

    public void alarmInterview() {
        Bson c = Filters.regex(DbKeyConfig.CHECK, Pattern.compile("0"));
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, c, null, 0, 0);
        List<TimeEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                TimeEntity calendar = TimeEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .check(AppUtils.parseString(doc.get(DbKeyConfig.CHECK)))
                        .nLoop(AppUtils.parseInt(doc.get(DbKeyConfig.N_LOOP)))
                        .build();
                calendars.add(calendar);
            }
        }
        for (TimeEntity calendar : calendars) {
            long differenceTime = calendar.getTime() - System.currentTimeMillis();
            int n = calendar.getNLoop();
            if (differenceTime <= timeCheck && differenceTime > 0) {
                Bson con = Filters.eq(DbKeyConfig.ID, calendar.getId());
                if (n != nLoop) {
                    n++;
                    // update roles
                    Bson updates = Updates.combine(
                            Updates.set(DbKeyConfig.N_LOOP, n)
                    );
                    db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, con, updates, true);
//                    sendEmail(calendar.getTime());
                } else {
                    Bson updates = Updates.combine(
                            Updates.set(DbKeyConfig.CHECK, "1")
                    );
                    db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, con, updates, true);
                }

            }
        }

    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
//
//    @Value("${gmail.account}")
//    private String fromEmail;
//    @Value("${gmail.password}")
//    private String password;
//
//    public void sendEmail(Long time) {
//        // dia chi email nguoi nhan
//        final String toEmail = "quanbn69@gmail.com";
//        final String subject = "ALARM INTERVIEW";
//        final String body = "You have an interview at "+time;
//        Properties props = new Properties();
//        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
//        props.put("mail.smtp.port", "587"); //TLS Port
//        props.put("mail.smtp.auth", "true"); //enable authentication
//        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
//        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(fromEmail, password);
//            }
//        });
//        MimeMessage message = new MimeMessage(session);
//        message.setFrom(new InternetAddress(fromEmail));
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
//        message.setSubject(subject);
//        // Phan 1 gom doan tin nhan
//        BodyPart messageBodyPart1 = new MimeBodyPart();
//        messageBodyPart1.setText(body);
//        Multipart multipart = new MimeMultipart();
//        multipart.addBodyPart(messageBodyPart1);
//        message.setContent(multipart);
//        Transport.send(message);
//    }


}
