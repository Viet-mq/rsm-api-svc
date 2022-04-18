package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.JobLevelEntity;
import com.edso.resume.api.domain.request.CreateJobLevelRequest;
import com.edso.resume.api.domain.request.DeleteJobLevelRequest;
import com.edso.resume.api.domain.request.UpdateJobLevelRequest;
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
public class JobLevelServiceImpl extends BaseService implements JobLevelService {

    public JobLevelServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<JobLevelEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_JOB_LEVEL, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<JobLevelEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                JobLevelEntity jobLevel = JobLevelEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .build();
                rows.add(jobLevel);
            }
        }
        GetArrayResponse<JobLevelEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_JOB_LEVEL, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createJobLevel(CreateJobLevelRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName();
            Bson c = Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            long count = db.countAll(CollectionNameDefs.COLL_JOB_LEVEL, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document jobLevel = new Document();
            jobLevel.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            jobLevel.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            jobLevel.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            jobLevel.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            jobLevel.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            jobLevel.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            jobLevel.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            jobLevel.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());
            jobLevel.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getMyOrganizations());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_JOB_LEVEL, jobLevel);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateJobLevel(UpdateJobLevelRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idJobLevel = Filters.eq(DbKeyConfig.LEVEL_JOB_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.LEVEL_JOB_NAME, AppUtils.mergeWhitespace(name))
            );
            db.update(CollectionNameDefs.COLL_PROFILE, idJobLevel, updateProfile);


            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_JOB_LEVEL, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteJobLevel(DeleteJobLevelRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document levelJob = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.LEVEL_JOB_ID, request.getId()));
            if (levelJob == null) {
                String id = request.getId();
                Bson cond = Filters.eq(DbKeyConfig.ID, id);
                Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, cond);

                if (idDocument == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
                db.delete(CollectionNameDefs.COLL_JOB_LEVEL, cond);
                response.setSuccess();
                return response;
            } else {
                response.setFailed("Không thể xóa cấp bậc công việc này!");
                return response;
            }
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
