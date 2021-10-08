package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.TimeEntity;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest;
import com.edso.resume.api.domain.request.DeleteCalendarProfileRequest;
import com.edso.resume.api.domain.request.UpdateCalendarProfileRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
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
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CalendarServiceImpl extends BaseService implements CalendarService {
    private final MongoDbOnlineSyncActions db;
    private final HistoryService historyService;
    private final BaseResponse response;

    @Value("${calendar.timeCheck}")
    private long timeCheck;

    @Value("${calendar.nLoop}")
    private int nLoop;

    public CalendarServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate) {
        super(db, rabbitTemplate);
        this.db = db;
        this.historyService = historyService;
        this.response = new BaseResponse();
    }

    @Override
    public GetArrayCalendarReponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile) {
        GetArrayCalendarReponse<CalendarEntity> resp = new GetArrayCalendarReponse<>();
        if (!validateDictionary(idProfile, CollectionNameDefs.COLL_PROFILE)) {
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.eq("idProfile", idProfile));
        }
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, null, 0, 0);
        List<CalendarEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CalendarEntity calendar = CalendarEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .time(AppUtils.parseLong(doc.get("time")))
                        .address(AppUtils.parseString(doc.get("address")))
                        .form(AppUtils.parseString(doc.get("form")))
                        .interviewer(parseList(doc.get("interviewer")))
                        .interviewee(AppUtils.parseString(doc.get("interviewee")))
                        .content(AppUtils.parseString(doc.get("content")))
                        .question(AppUtils.parseString(doc.get("question")))
                        .comments(AppUtils.parseString(doc.get("comments")))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
                        .status(AppUtils.parseString(doc.get("status")))
                        .reason(AppUtils.parseString(doc.get("reason")))
                        .timeStart(AppUtils.parseLong(doc.get("timeStart")))
                        .timeFinish(AppUtils.parseLong(doc.get("timeFinish")))
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

        String idProfile = request.getIdProfile();
        Bson cond = Filters.eq("id", idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idProfileDocument == null) {
            response.setFailed("Id profile không tồn tại");
            return response;
        }

        Document calendar = new Document();
        calendar.append("id", UUID.randomUUID().toString());
        calendar.append("idProfile", idProfile);
        calendar.append("time", request.getTime());
        calendar.append("address", request.getAddress());
        calendar.append("form", request.getForm());
        calendar.append("interviewer", request.getInterviewer());
        calendar.append("interviewee", request.getInterviewee());
        calendar.append("content", request.getContent());
        calendar.append("question", request.getQuestion());
        calendar.append("comments", request.getComments());
        calendar.append("evaluation", request.getEvaluation());
        calendar.append("status", request.getStatus());
        calendar.append("reason", request.getReason());
        calendar.append("timeStart", request.getTimeStart());
        calendar.append("timeFinish", request.getTimeFinish());
        calendar.append("check", "0");
        calendar.append("nLoop", 0);
        calendar.append("create_at", System.currentTimeMillis());
        calendar.append("update_at", System.currentTimeMillis());
        calendar.append("create_by", request.getInfo().getUsername());
        calendar.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, calendar);
        response.setSuccess();

        //Insert history to DB
        historyService.createHistory(idProfile, "Tạo lịch phỏng vấn", request.getInfo().getFullName());

        return response;

    }

    @Override
    public BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest request) {

        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String idProfile = request.getIdProfile();
        Bson con = Filters.eq("id", idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if (idProfileDocument == null) {
            response.setFailed("Id profile không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("ipProfile", idProfile),
                Updates.set("time", request.getTime()),
                Updates.set("address", request.getAddress()),
                Updates.set("form", request.getForm()),
                Updates.set("interviewer", request.getInterviewer()),
                Updates.set("interviewee", request.getInterviewee()),
                Updates.set("content", request.getContent()),
                Updates.set("question", request.getQuestion()),
                Updates.set("comments", request.getComments()),
                Updates.set("evaluation", request.getEvaluation()),
                Updates.set("status", request.getStatus()),
                Updates.set("reason", request.getReason()),
                Updates.set("timeStart", request.getTimeStart()),
                Updates.set("timeFinish", request.getTimeFinish()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, updates, true);
        response.setSuccess();

        //Insert history to DB
        historyService.createHistory(idProfile, "Sửa lịch phỏng vấn", request.getInfo().getFullName());

        return response;

    }

    @Override
    public BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request) {
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        //Insert history to DB
        historyService.createHistory(request.getIdProfile(), "Xóa lịch phỏng vấn", request.getInfo().getFullName());
        response.setSuccess();

        return response;
    }

    public void alarmInterview() {
        Bson c = Filters.regex("check", Pattern.compile("0"));
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, c, null, 0, 0);
        List<TimeEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                TimeEntity calendar = TimeEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .time(AppUtils.parseLong(doc.get("time")))
                        .check(AppUtils.parseString(doc.get("check")))
                        .nLoop(AppUtils.parseInt(doc.get("nLoop")))
                        .build();
                calendars.add(calendar);
            }
        }
        for (TimeEntity calendar : calendars) {
            long differenceTime = calendar.getTime() - System.currentTimeMillis();
            int n = calendar.getNLoop();
            if (differenceTime <= timeCheck && differenceTime > 0) {
                Bson con = Filters.eq("id", calendar.getId());
                if (n != nLoop) {
                    n++;
                    // update roles
                    Bson updates = Updates.combine(
                            Updates.set("nLoop", n)
                    );
                    db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, con, updates, true);
//                    sendEmail(calendar.getTime());
                } else {
                    Bson updates = Updates.combine(
                            Updates.set("check", "1")
                    );
                    db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, con, updates, true);
                }

            }
        }

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
