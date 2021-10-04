package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    private final MongoDbOnlineSyncActions db;
    private final HistoryService historyService;

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService) {
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
                        .id(AppUtils.parseString(doc.get("id")))
                        .fullName(AppUtils.parseString(doc.get("fullName")))
                        .dateOfBirth(AppUtils.parseLong(doc.get("dateOfBirth")))
                        .hometown(AppUtils.parseString(doc.get("hometown")))
                        .school(AppUtils.parseString(doc.get("school")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .job(AppUtils.parseString(doc.get("job")))
                        .levelJob(AppUtils.parseString(doc.get("levelJob")))
                        .cv(AppUtils.parseString(doc.get("cv")))
                        .sourceCV(AppUtils.parseString(doc.get("sourceCV")))
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
        Bson cond = Filters.regex("id", Pattern.compile(idProfile));
        Document one = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
        ProfileDetailEntity profile = ProfileDetailEntity.builder()
                .id(AppUtils.parseString(one.get("id")))
                .fullName(AppUtils.parseString(one.get("fullName")))
                .dateOfBirth(AppUtils.parseString(one.get("dateOfBirth")))
                .hometown(AppUtils.parseString(one.get("hometown")))
                .school(AppUtils.parseString(one.get("school")))
                .phoneNumber(AppUtils.parseString(one.get("phoneNumber")))
                .email(AppUtils.parseString(one.get("email")))
                .job(AppUtils.parseString(one.get("job")))
                .levelJob(AppUtils.parseString(one.get("levelJob")))
                .cv(AppUtils.parseString(one.get("cv")))
                .sourceCV(AppUtils.parseString(one.get("sourceCV")))
                .hrRef(AppUtils.parseString(one.get("hrRef")))
                .dateOfApply(AppUtils.parseString(one.get("dateOfApply")))
                .cvType(AppUtils.parseString(one.get("cvType")))
                .statusCV(AppUtils.parseString(one.get("statusCV")))
                .lastApply(AppUtils.parseString(one.get("lastApply")))
                .tags(AppUtils.parseString(one.get("tags")))
                .gender(AppUtils.parseString(one.get("gender")))
                .note(AppUtils.parseString(one.get("note")))
                .dateOfCreate(AppUtils.parseLong(one.get("create_at")))
                .dateOfUpdate(AppUtils.parseLong(one.get("update_at")))
                .evaluation(AppUtils.parseString(one.get("evaluation")))
                .build();

        GetReponse<ProfileDetailEntity> reponse = new GetReponse<>();
        reponse.setSuccess(profile);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(idProfile, System.currentTimeMillis(),"Select profile", info.getUsername());
        historyService.createHistory(createHistoryRequest);

        return reponse;
    }

    @Override
    public BaseResponse createProfile(CreateProfileRequest request)  {

        BaseResponse response = new BaseResponse();

        String idProfile = UUID.randomUUID().toString();

        Document profile = new Document();
        profile.append("id", idProfile);
        profile.append("fullName", request.getFullName());
        profile.append("dateOfBirth", request.getDateOfBirth());
        profile.append("hometown", request.getHometown());
        profile.append("school", request.getSchool());
        profile.append("phoneNumber", request.getPhoneNumber());
        profile.append("email", request.getEmail());
        profile.append("job", request.getJob());
        profile.append("levelJob", request.getLevelJob());
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

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(idProfile,System.currentTimeMillis(),"Create profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse updateProfile(UpdateProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
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

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Update profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        return response;

    }

    @Override
    public BaseResponse updateDetailProfile(UpdateDetailProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
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

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Update detail profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        return response;

    }

    @Override
    public BaseResponse deleteProfile(DeleteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Delete profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
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

        //Insert history to DB
        CreateHistoryRequest createHistoryRequest = new CreateHistoryRequest(id,System.currentTimeMillis(),"Update status profile",request.getInfo().getUsername());
        historyService.createHistory(createHistoryRequest);

        return response;
    }

}
