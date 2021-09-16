package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.request.CreateJobRequest;
import com.edso.resume.api.domain.request.DeleteJobRequest;
import com.edso.resume.api.domain.request.UpdateJobRequest;
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
import java.util.regex.Pattern;

@Service
public class JobServiceImpl extends BaseService implements JobService {

    private final MongoDbOnlineSyncActions db;

    public JobServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<CategoryEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_JOB, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_JOB, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<CategoryEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CategoryEntity category = CategoryEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .build();
                rows.add(category);
            }
        }
        GetArrayResponse<CategoryEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createJob(CreateJobRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_JOB, c);

        if (count > 0) {
            response.setFailed("Name already existed !");
            return response;
        }

        Document job = new Document();
        job.append("name_search", name.toLowerCase());
        job.append("name", name);
        job.append("create_at", System.currentTimeMillis());
        job.append("update_at", System.currentTimeMillis());
        job.append("create_by", request.getInfo().getUsername());
        job.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_JOB, job);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateJob(UpdateJobRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_JOB, Filters.eq("name_search", name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get("id"));
            if (!objId.equals(id)) {
                response.setFailed("Name already existed !");
                return response;
            }
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("name", name),
                Updates.set("name_search", name.toLowerCase()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_JOB, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteJob(DeleteJobRequest request) {
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        db.delete(CollectionNameDefs.COLL_JOB, cond);
        return new BaseResponse(0, "OK");
    }

}
