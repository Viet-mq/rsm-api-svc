package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.StatusCVEntity;
import com.edso.resume.api.domain.request.CreateStatusCVRequest;
import com.edso.resume.api.domain.request.DeleteStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateStatusCVRequest;
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
public class StatusCVServiceImpl extends BaseService implements StatusCVService {

    private final MongoDbOnlineSyncActions db;

    public StatusCVServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<StatusCVEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_STATUS_CV, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_STATUS_CV, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<StatusCVEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                StatusCVEntity statusCV = StatusCVEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .build();
                rows.add(statusCV);
            }
        }
        GetArrayResponse<StatusCVEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createStatusCV(CreateStatusCVRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_STATUS_CV, c);

        if (count > 0) {
            response.setFailed("Name already existed !");
            return response;
        }

        Document statusCV = new Document();
        statusCV.append("id", UUID.randomUUID().toString());
        statusCV.append("name", name);
        statusCV.append("name_search", name.toLowerCase());
        statusCV.append("create_at", System.currentTimeMillis());
        statusCV.append("update_at", System.currentTimeMillis());
        statusCV.append("create_by", request.getInfo().getUsername());
        statusCV.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_STATUS_CV, statusCV);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateStatusCV(UpdateStatusCVRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_STATUS_CV, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_STATUS_CV, Filters.eq("name_search", name.toLowerCase()));
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
        db.update(CollectionNameDefs.COLL_STATUS_CV, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteStatusCV(DeleteStatusCVRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_STATUS_CV, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_STATUS_CV, cond);
        return new BaseResponse(0, "OK");
    }

}