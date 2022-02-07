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
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_SCHOOL, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
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
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_SCHOOL, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createSchool(CreateSchoolRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_SCHOOL, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document school = new Document();
            school.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            school.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            school.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            school.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            school.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            school.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            school.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            school.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_SCHOOL, school);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateSchool(UpdateSchoolRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idSchool = Filters.eq(DbKeyConfig.SCHOOL_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.SCHOOL_NAME, AppUtils.mergeWhitespace(name.toLowerCase()))
            );
            db.update(CollectionNameDefs.COLL_PROFILE, idSchool, updateProfile, true);


            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_SCHOOL, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteSchool(DeleteSchoolRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document school = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.SCHOOL_ID, request.getId()));
            if (school == null) {
                String id = request.getId();
                Bson cond = Filters.eq(DbKeyConfig.ID, id);
                Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

                if (idDocument == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
                db.delete(CollectionNameDefs.COLL_SCHOOL, cond);
                response.setSuccess();
                return response;
            } else {
                response.setFailed("Không thể xóa trường học này!");
                return response;
            }
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

}
