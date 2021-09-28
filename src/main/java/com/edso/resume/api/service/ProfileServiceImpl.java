package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    private final MongoDbOnlineSyncActions db;
    public ProfileServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, String idProfile, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex("name_search", Pattern.compile(fullName.toLowerCase())));
        }
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("id", Pattern.compile(idProfile)));
            //Insert history to DB
            createHistory(idProfile,"Select", info.getUsername(), db);
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
                        .dateOfBirth(AppUtils.parseString(doc.get("dateOfBirth")))
                        .hometown(AppUtils.parseString(doc.get("hometown")))
                        .school(AppUtils.parseString(doc.get("school")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .job(AppUtils.parseString(doc.get("job")))
                        .levelJob(AppUtils.parseString(doc.get("levelJob")))
                        .cv(AppUtils.parseString(doc.get("cv")))
                        .sourceCV(AppUtils.parseString(doc.get("sourceCV")))
                        .hrRef(AppUtils.parseString(doc.get("hrRef")))
                        .dateOfApply(AppUtils.parseString(doc.get("dateOfApply")))
                        .cvType(AppUtils.parseString(doc.get("cvType")))
                        .statusCV(AppUtils.parseString(doc.get("statusCV")))
                        .lastApply(AppUtils.parseString(doc.get("lastApply")))
                        .tags(AppUtils.parseString(doc.get("tags")))
                        .gender(AppUtils.parseString(doc.get("gender")))
                        .note(AppUtils.parseString(doc.get("note")))
                        .dateOfCreate(parseDate(AppUtils.parseLong(doc.get("create_at"))))
                        .dateOfUpdate(parseDate(AppUtils.parseLong(doc.get("update_at"))))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
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
    public GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_HISTORY_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_HISTORY_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<HistoryEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                HistoryEntity history = HistoryEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .time(parseDate(AppUtils.parseLong(doc.get("time"))))
                        .action(AppUtils.parseString(doc.get("action")))
                        .by(AppUtils.parseString(doc.get("by")))
                        .build();
                rows.add(history);
            }
        }
        GetArrayResponse<HistoryEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
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
        createHistory(idProfile,"Create",request.getInfo().getUsername(),db);

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
                Updates.set("tags", request.getTags()),
                Updates.set("note", request.getNote()),
                Updates.set("gender", request.getGender()),
                Updates.set("lastApply", request.getLastApply()),
                Updates.set("evaluation", request.getEvaluation()),
                Updates.set("name_search", request.getFullName().toLowerCase()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername()),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

        //Insert history to DB
        createHistory(id,"Update",request.getInfo().getUsername(), db);

        response.setSuccess();
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
        createHistory(id,"Delete",request.getInfo().getUsername(), db);

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

        //Insert history to DB
        createHistory(id,"Update status",request.getInfo().getUsername(), db);

        response.setSuccess();
        return response;
    }

}
