package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.request.CreateJobRequest;
import com.edso.resume.api.domain.request.DeleteJobRequest;
import com.edso.resume.api.domain.request.UpdateJobRequest;
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
public class JobServiceImpl extends BaseService implements JobService {

    public JobServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<CategoryEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_JOB, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<CategoryEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CategoryEntity category = CategoryEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .build();
                rows.add(category);
            }
        }
        GetArrayResponse<CategoryEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_JOB, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createJob(CreateJobRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName().trim();
            Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            long count = db.countAll(CollectionNameDefs.COLL_JOB, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document job = new Document();
            job.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            job.append(DbKeyConfig.NAME, name);
            job.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            job.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            job.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            job.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            job.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_JOB, job);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateJob(UpdateJobRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_JOB, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idJob = Filters.eq(DbKeyConfig.JOB_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.JOB_NAME, request.getName())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, idJob, updateProfile, true);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, name),
                    Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_JOB, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteJob(DeleteJobRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_JOB, cond);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        return new BaseResponse(0, "OK");
    }

}
