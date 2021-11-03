package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.SchoolEntity;
import com.edso.resume.api.domain.request.CreateSchoolRequest;
import com.edso.resume.api.domain.request.DeleteSchoolRequest;
import com.edso.resume.api.domain.request.UpdateSchoolRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
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

    public SchoolServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<SchoolEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_SCHOOL, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_SCHOOL, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<SchoolEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                SchoolEntity school = SchoolEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
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
        Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_SCHOOL, c);

        if (count > 0) {
            response.setFailed("Tên này đã tồn tại");
            return response;
        }

        Document school = new Document();
        school.append(DbKeyConfig.ID, UUID.randomUUID().toString());
        school.append(DbKeyConfig.NAME, name);
        school.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
        school.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
        school.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
        school.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
        school.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_SCHOOL, school);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateSchool(UpdateSchoolRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
            if (!objId.equals(id)) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }
        }

        Bson idSchool = Filters.eq(DbKeyConfig.SCHOOL_ID, request.getId());
        Bson updateProfile = Updates.combine(
                Updates.set(DbKeyConfig.SCHOOL_NAME, request.getName())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, idSchool, updateProfile, true);


        // update roles
        Bson updates = Updates.combine(
                Updates.set(DbKeyConfig.NAME, name),
                Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_SCHOOL, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteSchool(DeleteSchoolRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }
        db.delete(CollectionNameDefs.COLL_SCHOOL, cond);
        return new BaseResponse(0, "OK");
    }

}
