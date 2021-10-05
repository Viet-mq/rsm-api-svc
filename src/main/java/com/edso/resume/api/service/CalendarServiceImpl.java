package com.edso.resume.api.service;

import com.edso.resume.api.domain.Object.Comment;
import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.TimeEntity;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest;
import com.edso.resume.api.domain.request.CreateHistoryRequest;
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

    @Value("${calendar.timeCheck}")
    private long timeCheck;

    @Value("${calendar.nLoop}")
    private int nLoop;

    public CalendarServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService) {
        this.db = db;
        this.historyService = historyService;
    }

    @Override
    public GetArrayCalendarReponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
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
                        .question(parseList(doc.get("question")))
                        .comment(parseListComment(doc.get("comments")))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
                        .status(AppUtils.parseString(doc.get("status")))
                        .reason(AppUtils.parseString(doc.get("reason")))
                        .timeStart(AppUtils.parseLong(doc.get("timeStart")))
                        .timeFinish(AppUtils.parseLong(doc.get("timeFinish")))
                        .build();
                calendars.add(calendar);
            }
        }
        GetArrayCalendarReponse<CalendarEntity> resp = new GetArrayCalendarReponse<>();
        resp.setSuccess();
        resp.setCalendars(calendars);
        return resp;
    }

    @Override
    public BaseResponse createCalendarProfile(CreateCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();

        List<Comment> lst = request.getComments();
        List<Document> lstComment = new ArrayList<>();

        for (Comment c : lst) {
            Document comment = new Document();
            comment.append("name", c.getName());
            comment.append("content", c.getContent());
            lstComment.add(comment);
        }

        String idProfile = request.getIdProfile();

        Document profile = new Document();
        profile.append("id", UUID.randomUUID().toString());
        profile.append("idProfile", idProfile);
        profile.append("time", request.getTime());
        profile.append("address", request.getAddress());
        profile.append("form", request.getForm());
        profile.append("interviewer", request.getInterviewer());
        profile.append("interviewee", request.getInterviewee());
        profile.append("content", request.getContent());
        profile.append("question", request.getQuestion());
        profile.append("comments", lstComment);
        profile.append("evaluation", request.getEvaluation());
        profile.append("status", request.getStatus());
        profile.append("reason", request.getReason());
        profile.append("timeStart", request.getTimeStart());
        profile.append("timeFinish", request.getTimeFinish());
        profile.append("check", "0");
        profile.append("nLoop", 0);
        profile.append("create_at", System.currentTimeMillis());
        profile.append("update_at", System.currentTimeMillis());
        profile.append("create_by", request.getInfo().getUsername());
        profile.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, profile);
        response.setSuccess();

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(idProfile, System.currentTimeMillis(), "Tạo lịch phỏng vấn", request.getInfo().getFullName());
        historyService.createHistory(createHistoryRequest);

        return response;

    }

    @Override
    public BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String idProfile = request.getIdProfile();

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
                Updates.set("comment", request.getComment()),
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
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(idProfile, System.currentTimeMillis(), "Sửa lịch phỏng vấn", request.getInfo().getFullName());
        historyService.createHistory(createHistoryRequest);

        return response;

    }

    @Override
    public BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(request.getIdProfile(), System.currentTimeMillis(), "Xóa lịch phỏng vấn", request.getInfo().getFullName());
        historyService.createHistory(createHistoryRequest);

        return new BaseResponse(0, "OK");
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
