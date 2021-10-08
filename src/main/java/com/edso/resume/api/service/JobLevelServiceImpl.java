package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.JobLevelEntity;
import com.edso.resume.api.domain.request.CreateJobLevelRequest;
import com.edso.resume.api.domain.request.DeleteJobLevelRequest;
import com.edso.resume.api.domain.request.UpdateJobLevelRequest;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class JobLevelServiceImpl extends BaseService implements JobLevelService {
    private final MongoDbOnlineSyncActions db;

    public JobLevelServiceImpl(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate) {
        super(db, rabbitTemplate);
        this.db = db;
    }

    @Override
    public GetArrayResponse<JobLevelEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_JOB_LEVEL, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_JOB_LEVEL, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<JobLevelEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                JobLevelEntity jobLevel = JobLevelEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .build();
                rows.add(jobLevel);
            }
        }
        GetArrayResponse<JobLevelEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createJobLevel(CreateJobLevelRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_JOB_LEVEL, c);

        if (count > 0) {
            response.setFailed("Tên này đã tồn tại");
            return response;
        }

        Document jobLevel = new Document();
        jobLevel.append("id", UUID.randomUUID().toString());
        jobLevel.append("name", name);
        jobLevel.append("name_search", name.toLowerCase());
        jobLevel.append("create_at", System.currentTimeMillis());
        jobLevel.append("update_at", System.currentTimeMillis());
        jobLevel.append("create_by", request.getInfo().getUsername());
        jobLevel.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_JOB_LEVEL, jobLevel);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateJobLevel(UpdateJobLevelRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, Filters.eq("name_search", name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get("id"));
            if (!objId.equals(id)) {
                response.setFailed("Tên này đã tồn tại");
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
        db.update(CollectionNameDefs.COLL_JOB_LEVEL, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteJobLevel(DeleteJobLevelRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }
        db.delete(CollectionNameDefs.COLL_JOB_LEVEL, cond);
        return new BaseResponse(0, "OK");
    }
}
