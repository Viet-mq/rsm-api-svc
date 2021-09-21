package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
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
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    private final MongoDbOnlineSyncActions db;
    public ProfileServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
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
                        .phonenumber(AppUtils.parseString(doc.get("phonenumber")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .job(AppUtils.parseString(doc.get("job")))
                        .levelJob(AppUtils.parseString(doc.get("levelJob")))
                        .cv(AppUtils.parseString(doc.get("cv")))
                        .sourceCV(AppUtils.parseString(doc.get("sourceCV")))
                        .hrRef(AppUtils.parseString(doc.get("hrRef")))
                        .dateOfApply(AppUtils.parseString(doc.get("dateOfApply")))
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
    public BaseResponse createProfile(CreateProfileRequest request)  {

        BaseResponse response = new BaseResponse();

//        String email = request.getEmail();
//        Bson c = Filters.eq("email", email);
//        long count = db.countAll(CollectionNameDefs.COLL_PROFILE, c);
//
//        if (count > 0) {
//            response.setFailed("Email already existed !");
//            return response;
//        }

        Document job = new Document();
        job.append("id", UUID.randomUUID().toString());
        job.append("fullName", request.getFullName());
        job.append("dateOfBirth", request.getDateOfBirth());
        job.append("hometown", request.getHometown());
        job.append("school", request.getSchool());
        job.append("phonenumber", request.getPhonenumber());
        job.append("email", request.getEmail());
        job.append("job", request.getJob());
        job.append("levelJob", request.getLevelJob());
        job.append("cv", request.getCv());
        job.append("sourceCV", request.getSourceCV());
        job.append("hrRef", request.getHrRef());
        job.append("dateOfApply", request.getDateOfApply());
        job.append("cvType", request.getCvType());
        job.append("name_search", request.getFullName().toLowerCase());
        job.append("create_at", System.currentTimeMillis());
        job.append("update_at", System.currentTimeMillis());
        job.append("create_by", request.getInfo().getUsername());
        job.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_PROFILE, job);

        response.setSuccess();
        return response;

    }

//    public Long getMillisecondOfDate(String date) throws ParseException {
//        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//        Date d = df.parse(date);
//        return d.getTime();
//    }

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

//        String email = request.getEmail();
//        Document obj = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq("name_search", email));
//        if (obj != null) {
//            String objId = AppUtils.parseString(obj.get("id"));
//            if (!objId.equals(id)) {
//                response.setFailed("Email already existed !");
//                return response;
//            }
//        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("fullName", request.getFullName()),
                Updates.set("dateOfBirth", request.getDateOfBirth()),
                Updates.set("hometown", request.getHometown()),
                Updates.set("school", request.getSchool()),
                Updates.set("phonenumber", request.getPhonenumber()),
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
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

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
        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse changeStatusCV(ChangeStatusCVRequest request) {
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
                Updates.set("change_statuscv_at", System.currentTimeMillis()),
                Updates.set("change_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

        response.setSuccess();
        return response;
    }
}
