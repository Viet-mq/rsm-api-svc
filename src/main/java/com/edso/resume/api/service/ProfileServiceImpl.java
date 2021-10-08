package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    private final MongoDbOnlineSyncActions db;
    private final HistoryService historyService;
    private final BaseResponse response = new BaseResponse();

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate) {
        super(db, rabbitTemplate);
        this.db = db;
        this.historyService = historyService;
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
                ProfileEntity profile = ProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .dateOfBirth(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_BIRTH)))
                        .hometown(AppUtils.parseString(doc.get(DbKeyConfig.HOMETOWN)))
                        .schoolId(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_ID)))
                        .schoolName(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_NAME)))
                        .phoneNumber(AppUtils.parseString(doc.get(DbKeyConfig.PHONE_NUMBER)))
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
        if (!validateDictionary(idProfile, CollectionNameDefs.COLL_PROFILE)) {
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
        historyService.createHistory(idProfile, "Xem chi tiết profile", info.getFullName());

        return response;
    }

    @Override
    public BaseResponse createProfile(CreateProfileRequest request) {

        String idProfile = UUID.randomUUID().toString();


        //Validate
        if (!validateDictionary(request.getJob(), CollectionNameDefs.COLL_JOB)) {
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getLevelJob(), CollectionNameDefs.COLL_JOB_LEVEL)) {
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        Document school = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq("id", request.getSchool()));
        if (school == null) {
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getSourceCV(), CollectionNameDefs.COLL_SOURCE_CV)) {
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // conventions
        Document profile = new Document();
        profile.append(DbKeyConfig.ID, idProfile);
        profile.append(DbKeyConfig.FULL_NAME, request.getFullName());
        profile.append(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth());
        profile.append(DbKeyConfig.HOMETOWN, request.getHometown());
        profile.append(DbKeyConfig.SCHOOL_ID, request.getSchool());
        profile.append(DbKeyConfig.SCHOOL_NAME, school.get(DbKeyConfig.NAME));

        profile.append("phone_number", request.getPhoneNumber());
        profile.append("email", request.getEmail());
        profile.append("job", request.getJob());

        profile.append("level_job_id", request.getLevelJob());

        profile.append("cv", request.getCv());
        profile.append("sourceCV", request.getSourceCV());
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
        insertToRabbitMQ("create profile", profile);

        //Insert history to DB
        historyService.createHistory(idProfile, "Tạo profile", request.getInfo().getFullName());

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse updateProfile(UpdateProfileRequest request) {

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getJob(), CollectionNameDefs.COLL_JOB)) {
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getLevelJob(), CollectionNameDefs.COLL_JOB_LEVEL)) {
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getSchool(), CollectionNameDefs.COLL_SCHOOL)) {
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getSourceCV(), CollectionNameDefs.COLL_SOURCE_CV)) {
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("fullName", request.getFullName()),
                Updates.set("dateOfBirth", request.getDateOfBirth()),
                Updates.set("hometown", request.getHometown()),
                Updates.set("school", request.getSchool()),
                Updates.set("phoneNumber", request.getPhoneNumber()),
                Updates.set("email", request.getEmail()),
                Updates.set("job", request.getJob()),
                Updates.set("levelJob", request.getLevelJob()),
                Updates.set("cv", request.getCv()),
                Updates.set("sourceCV", request.getSourceCV()),
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
        insertToRabbitMQ("update profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Sửa profile", request.getInfo().getFullName());

        return response;

    }

    @Override
    public BaseResponse updateDetailProfile(UpdateDetailProfileRequest request) {

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getJob(), CollectionNameDefs.COLL_JOB)) {
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getLevelJob(), CollectionNameDefs.COLL_JOB_LEVEL)) {
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getSchool(), CollectionNameDefs.COLL_SCHOOL)) {
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getSourceCV(), CollectionNameDefs.COLL_SOURCE_CV)) {
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("fullName", request.getFullName()),
                Updates.set("dateOfBirth", request.getDateOfBirth()),
                Updates.set("hometown", request.getHometown()),
                Updates.set("school", request.getSchool()),
                Updates.set("phoneNumber", request.getPhoneNumber()),
                Updates.set("email", request.getEmail()),
                Updates.set("job", request.getJob()),
                Updates.set("levelJob", request.getLevelJob()),
                Updates.set("cv", request.getCv()),
                Updates.set("sourceCV", request.getSourceCV()),
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
        insertToRabbitMQ("update detail profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Sửa chi tiết profile", request.getInfo().getFullName());

        return response;

    }

    @Override
    public BaseResponse deleteProfile(DeleteProfileRequest request) {
        BaseResponse response = new BaseResponse();

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        // insert to rabbitmq
        insertToRabbitMQ("delete profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Xóa profile", request.getInfo().getFullName());

        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq("id", id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        if (!validateDictionary(request.getStatusCV(), CollectionNameDefs.COLL_STATUS_CV)) {
            response.setFailed("Trạng thái cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("statusCV", request.getStatusCV()),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
        response.setSuccess();

        // insert to rabbitmq
        insertToRabbitMQ("update status profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Cập nhật trạng thái profile", request.getInfo().getFullName());

        return response;
    }


}
