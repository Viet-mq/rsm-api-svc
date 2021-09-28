package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CalendarServiceImpl extends BaseService implements CalendarService{
    private final MongoDbOnlineSyncActions db;

    public CalendarServiceImpl(MongoDbOnlineSyncActions db){
        this.db = db;
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
                        .time(AppUtils.parseString(doc.get("time")))
                        .address(AppUtils.parseString(doc.get("address")))
                        .form(AppUtils.parseString(doc.get("form")))
                        .interviewer(parseList(doc.get("interviewer")))
                        .interviewee(AppUtils.parseString(doc.get("interviewee")))
                        .content(AppUtils.parseString(doc.get("content")))
                        .question(parseList(doc.get("question")))
                        .comment(parseList(doc.get("comment")))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
                        .status(AppUtils.parseString(doc.get("status")))
                        .reason(AppUtils.parseString(doc.get("reason")))
                        .timeStart(AppUtils.parseString(doc.get("timeStart")))
                        .timeFinish(AppUtils.parseString(doc.get("timeFinish")))
                        .build();
                calendars.add(calendar);
            }
        }
        GetArrayCalendarReponse<CalendarEntity> resp = new GetArrayCalendarReponse<>();
        resp.setSuccess();
        resp.setCalendars(calendars);
        return resp;
    }

    @SuppressWarnings (value="unchecked")
    public List<String> parseList(Object list){
        return (List<String>) list;
    }

    @Override
    public BaseResponse createCalendarProfile(CreateCalendarProfileRequest request)  {

        BaseResponse response = new BaseResponse();

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
        profile.append("comment", request.getComment());
        profile.append("evaluation", request.getEvaluation());
        profile.append("status", request.getStatus());
        profile.append("reason", request.getReason());
        profile.append("timeStart", request.getTimeStart());
        profile.append("timeFinish", request.getTimeFinish());
        profile.append("create_at", System.currentTimeMillis());
        profile.append("update_at", System.currentTimeMillis());
        profile.append("create_by", request.getInfo().getUsername());
        profile.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, profile);

        //Insert history to DB
        createHistory(idProfile,"Create calendar",request.getInfo().getUsername(),db);

        response.setSuccess();
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

        // update roles
        Bson updates = Updates.combine(
                Updates.set("ipProfile", request.getIdProfile()),
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

        //Insert history to DB
        createHistory(request.getIdProfile(),"Update calendar",request.getInfo().getUsername(),db);

        response.setSuccess();
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
        createHistory(id,"Delete calendar",request.getInfo().getUsername(),db);

        return new BaseResponse(0, "OK");
    }
}
