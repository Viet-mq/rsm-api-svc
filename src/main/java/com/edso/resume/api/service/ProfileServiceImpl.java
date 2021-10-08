package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.*;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetReponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.routingkey}")
    private String routingkey;

    private final MongoDbOnlineSyncActions db;
    private final HistoryService historyService;
    private final RabbitTemplate rabbitTemplate;

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate) {
        this.db = db;
        this.historyService = historyService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex("name_search", Pattern.compile(fullName.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ProfileEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {

                String idSchool = AppUtils.parseString(doc.get("school"));
                Document schoolDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq("id",idSchool));

                String job = AppUtils.parseString(doc.get("job"));
                Document jobDocument = db.findOne(CollectionNameDefs.COLL_JOB, Filters.eq("id",job));

                String jobLevel = AppUtils.parseString(doc.get("levelJob"));
                Document jobLevelDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, Filters.eq("id",jobLevel));

                String sourceCV = AppUtils.parseString(doc.get("sourceCV"));
                Document sourceCVDocument = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, Filters.eq("id",sourceCV));

                ProfileEntity profile = ProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .fullName(AppUtils.parseString(doc.get("fullName")))
                        .dateOfBirth(AppUtils.parseLong(doc.get("dateOfBirth")))
                        .hometown(AppUtils.parseString(doc.get("hometown")))
                        .school(AppUtils.parseString(schoolDocument.get("name")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .job(AppUtils.parseString(jobDocument.get("name")))
                        .levelJob(AppUtils.parseString(jobLevelDocument.get("name")))
                        .cv(AppUtils.parseString(doc.get("cv")))
                        .sourceCV(AppUtils.parseString(sourceCVDocument.get("name")))
                        .hrRef(AppUtils.parseString(doc.get("hrRef")))
                        .dateOfApply(AppUtils.parseLong(doc.get("dateOfApply")))
                        .cvType(AppUtils.parseString(doc.get("cvType")))
                        .statusCV(AppUtils.parseString(doc.get("statusCV")))
                        .build();
                rows.add(profile);
            }
        }

        GetArrayResponse<ProfileEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);

        return resp;
    }

    @Override
    public GetReponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile) {

        GetReponse<ProfileDetailEntity> response = new GetReponse<>();

        //Validate
        Bson con = Filters.eq("id", idProfile);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if (idDocument == null) {
            response.setFailed("Id profile này không tồn tại");
            return response;
        }

        Bson cond = Filters.regex("id", Pattern.compile(idProfile));
        Document one = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
        ProfileDetailEntity profile = ProfileDetailEntity.builder()
                .id(AppUtils.parseString(one.get("id")))
                .fullName(AppUtils.parseString(one.get("fullName")))
                .dateOfBirth(AppUtils.parseLong(one.get("dateOfBirth")))
                .hometown(AppUtils.parseString(one.get("hometown")))
                .school(AppUtils.parseString(one.get("school")))
                .phoneNumber(AppUtils.parseString(one.get("phoneNumber")))
                .email(AppUtils.parseString(one.get("email")))
                .job(AppUtils.parseString(one.get("job")))
                .levelJob(AppUtils.parseString(one.get("levelJob")))
                .cv(AppUtils.parseString(one.get("cv")))
                .sourceCV(AppUtils.parseString(one.get("sourceCV")))
                .hrRef(AppUtils.parseString(one.get("hrRef")))
                .dateOfApply(AppUtils.parseLong(one.get("dateOfApply")))
                .cvType(AppUtils.parseString(one.get("cvType")))
                .statusCV(AppUtils.parseString(one.get("statusCV")))
                .lastApply(AppUtils.parseLong(one.get("lastApply")))
                .tags(AppUtils.parseString(one.get("tags")))
                .gender(AppUtils.parseString(one.get("gender")))
                .note(AppUtils.parseString(one.get("note")))
                .dateOfCreate(AppUtils.parseLong(one.get("create_at")))
                .dateOfUpdate(AppUtils.parseLong(one.get("update_at")))
                .evaluation(AppUtils.parseString(one.get("evaluation")))
                .build();

        response.setSuccess(profile);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(idProfile, System.currentTimeMillis(),"Xem chi tiết profile", info.getUsername());
        historyService.createHistory(createHistoryRequest);

        return response;
    }

    @Override
    public BaseResponse createProfile(CreateProfileRequest request) {

        BaseResponse response = new BaseResponse();

        String idProfile = UUID.randomUUID().toString();

        //Validate
        String job = request.getJob();
        Bson conJob = Filters.eq("id", job);
        Document jobDocument = db.findOne(CollectionNameDefs.COLL_JOB, conJob);

        if(jobDocument == null){
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        String levelJob = request.getLevelJob();
        Bson conLevelJob = Filters.eq("id", levelJob);
        Document levelJobDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, conLevelJob);

        if(levelJobDocument == null){
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        String school = request.getSchool();
        Bson conSchool = Filters.eq("id", school);
        Document schoolDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, conSchool);

        if(schoolDocument == null){
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        String sourceCV = request.getSourceCV();
        Bson conSourceCV = Filters.eq("id", sourceCV);
        Document sourceCVDocument = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, conSourceCV);

        if(sourceCVDocument == null){
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        Document profile = new Document();
        profile.append("id", idProfile);
        profile.append("fullName", request.getFullName());
        profile.append("dateOfBirth", request.getDateOfBirth());
        profile.append("hometown", request.getHometown());
        profile.append("school", school);
        profile.append("phoneNumber", request.getPhoneNumber());
        profile.append("email", request.getEmail());
        profile.append("job", job);
        profile.append("levelJob", levelJob);
        profile.append("cv", request.getCv());
        profile.append("sourceCV", sourceCV);
        profile.append("hrRef", request.getHrRef());
        profile.append("dateOfApply", request.getDateOfApply());
        profile.append("cvType", request.getCvType());
        profile.append("name_search", request.getFullName().toLowerCase());
        profile.append("create_at", System.currentTimeMillis());
        profile.append("update_at", System.currentTimeMillis());
        profile.append("update_statuscv_at", System.currentTimeMillis());
        profile.append("create_by", request.getInfo().getUsername());
        profile.append("update_by", request.getInfo().getUsername());
        profile.append("update_statuscv_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_PROFILE, profile);

        // insert to rabbitmq
        EventEntity event = new EventEntity("create profile", profile);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(idProfile,System.currentTimeMillis(),"Tạo profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse updateProfile(UpdateProfileRequest request){

        BaseResponse response = new BaseResponse();

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String job = request.getJob();
        Bson conJob = Filters.eq("id", job);
        Document jobDocument = db.findOne(CollectionNameDefs.COLL_JOB, conJob);

        if(jobDocument == null){
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        String levelJob = request.getLevelJob();
        Bson conLevelJob = Filters.eq("id", levelJob);
        Document levelJobDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, conLevelJob);

        if(levelJobDocument == null){
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        String school = request.getSchool();
        Bson conSchool = Filters.eq("id", school);
        Document schoolDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, conSchool);

        if(schoolDocument == null){
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        String sourceCV = request.getSourceCV();
        Bson conSourceCV = Filters.eq("id", sourceCV);
        Document sourceCVDocument = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, conSourceCV);

        if(sourceCVDocument == null){
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("fullName", request.getFullName()),
                Updates.set("dateOfBirth", request.getDateOfBirth()),
                Updates.set("hometown", request.getHometown()),
                Updates.set("school", school),
                Updates.set("phoneNumber", request.getPhoneNumber()),
                Updates.set("email", request.getEmail()),
                Updates.set("job", job),
                Updates.set("levelJob", levelJob),
                Updates.set("cv", request.getCv()),
                Updates.set("sourceCV", sourceCV),
                Updates.set("hrRef", request.getHrRef()),
                Updates.set("dateOfApply", request.getDateOfApply()),
                Updates.set("cvType", request.getCvType()),
                Updates.set("name_search", request.getFullName().toLowerCase()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername()),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
        response.setSuccess();

        // insert to rabbitmq
        EventEntity event = new EventEntity("update profile", request);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id, System.currentTimeMillis(),"Sửa profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        return response;

    }

    @Override
    public BaseResponse updateDetailProfile(UpdateDetailProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        //Validate
        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String job = request.getJob();
        Bson conJob = Filters.eq("id", job);
        Document jobDocument = db.findOne(CollectionNameDefs.COLL_JOB, conJob);

        if(jobDocument == null){
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        String levelJob = request.getLevelJob();
        Bson conLevelJob = Filters.eq("id", levelJob);
        Document levelJobDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, conLevelJob);

        if(levelJobDocument == null){
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        String school = request.getSchool();
        Bson conSchool = Filters.eq("id", school);
        Document schoolDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, conSchool);

        if(schoolDocument == null){
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        String sourceCV = request.getSourceCV();
        Bson conSourceCV = Filters.eq("id", sourceCV);
        Document sourceCVDocument = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, conSourceCV);

        if(sourceCVDocument == null){
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("fullName", request.getFullName()),
                Updates.set("dateOfBirth", request.getDateOfBirth()),
                Updates.set("hometown", request.getHometown()),
                Updates.set("school", school),
                Updates.set("phoneNumber", request.getPhoneNumber()),
                Updates.set("email", request.getEmail()),
                Updates.set("job", job),
                Updates.set("levelJob", levelJob),
                Updates.set("cv", request.getCv()),
                Updates.set("sourceCV", sourceCV),
                Updates.set("hrRef", request.getHrRef()),
                Updates.set("dateOfApply", request.getDateOfApply()),
                Updates.set("cvType", request.getCvType()),
                Updates.set("name_search", request.getFullName().toLowerCase()),
                Updates.set("tags", request.getTags()),
                Updates.set("note", request.getNote()),
                Updates.set("gender", request.getGender()),
                Updates.set("lastApply", request.getLastApply()),
                Updates.set("evaluation", request.getEvaluation()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername()),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
        response.setSuccess();

        // insert to rabbitmq
        EventEntity event = new EventEntity("update detail profile", request);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Sửa chi tiết profile",request.getInfo().getFullName());
        historyService.createHistory(createHistoryRequest);

        return response;

    }

    @Override
    public BaseResponse deleteProfile(DeleteProfileRequest request) {
        BaseResponse response = new BaseResponse();

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        // insert to rabbitmq
        EventEntity event = new EventEntity("delete profile", request);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Xóa profile",request.getInfo().getFullName());
        historyService.createHistory(createHistoryRequest);

        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {
        BaseResponse response = new BaseResponse();

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String statusCV = request.getStatusCV();
        Bson conStatusCV = Filters.eq("id", statusCV);
        Document statusCVDocument = db.findOne(CollectionNameDefs.COLL_STATUS_CV, conStatusCV);

        if(statusCVDocument == null){
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("statusCV", statusCV),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
        response.setSuccess();

        // insert to rabbitmq
        EventEntity event = new EventEntity("update status profile", request);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Cập nhật trạng thái profile",request.getInfo().getFullName());
        historyService.createHistory(createHistoryRequest);

        return response;
    }

}
