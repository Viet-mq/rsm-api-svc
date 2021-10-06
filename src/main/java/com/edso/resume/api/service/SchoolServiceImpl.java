package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.entities.SchoolEntity;
import com.edso.resume.api.domain.request.CreateSchoolRequest;
import com.edso.resume.api.domain.request.DeleteSchoolRequest;
import com.edso.resume.api.domain.request.UpdateSchoolRequest;
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
public class SchoolServiceImpl extends BaseService implements SchoolService {

    private final MongoDbOnlineSyncActions db;

    public SchoolServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<SchoolEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_SCHOOL, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_SCHOOL, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<SchoolEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                SchoolEntity school = SchoolEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .build();
                rows.add(school);
            }
        }
        GetArrayResponse<SchoolEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createSchool(CreateSchoolRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_SCHOOL, c);

        if (count > 0) {
            response.setFailed("Name already existed !");
            return response;
        }

        Document school = new Document();
        school.append("id", UUID.randomUUID().toString());
        school.append("name", name);
        school.append("name_search", name.toLowerCase());
        school.append("create_at", System.currentTimeMillis());
        school.append("update_at", System.currentTimeMillis());
        school.append("create_by", request.getInfo().getUsername());
        school.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_SCHOOL, school);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateSchool(UpdateSchoolRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq("name_search", name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get("id"));
            if (!objId.equals(id)) {
                response.setFailed("Name already existed !");
                return response;
            }
        }

        SchoolEntity school = SchoolEntity.builder()
                .id(AppUtils.parseString(idDocument.get("id")))
                .name(AppUtils.parseString(idDocument.get("name")))
                .build();

        String oldName = school.getName();
        String newName = request.getName();

        Bson conOldName = Filters.eq("school", oldName);

        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, conOldName, null, 0, 0);
        List<ProfileEntity> list = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ProfileEntity profile = ProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .build();
                list.add(profile);
            }
        }

        for (ProfileEntity profile : list) {
            Bson conProfile = Filters.eq("id", profile.getId());
            Bson updateSchool = Updates.combine(
                    Updates.set("school", newName)
            );

            db.update(CollectionNameDefs.COLL_PROFILE, conProfile, updateSchool, true);
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("name", name),
                Updates.set("name_search", name.toLowerCase()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_SCHOOL, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteSchool(DeleteSchoolRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }
        db.delete(CollectionNameDefs.COLL_SCHOOL, cond);
        return new BaseResponse(0, "OK");
    }

}
