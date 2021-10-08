package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DepartmentEntity;
import com.edso.resume.api.domain.request.CreateDepartmentRequest;
import com.edso.resume.api.domain.request.DeleteDepartmentRequest;
import com.edso.resume.api.domain.request.UpdateDepartmentRequest;
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
public class DepartmentServiceImpl extends BaseService implements DepartmentService {
    private final MongoDbOnlineSyncActions db;

    public DepartmentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
        this.db = db;
    }

    @Override
    public GetArrayResponse<DepartmentEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_DEPARTMENT, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_DEPARTMENT, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<DepartmentEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                DepartmentEntity department = DepartmentEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .build();
                rows.add(department);
            }
        }
        GetArrayResponse<DepartmentEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createDepartment(CreateDepartmentRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_DEPARTMENT, c);

        if (count > 0) {
            response.setFailed("Tên này đã tồn tại");
            return response;
        }

        Document department = new Document();
        department.append("id", UUID.randomUUID().toString());
        department.append("name", name);
        department.append("name_search", name.toLowerCase());
        department.append("create_at", System.currentTimeMillis());
        department.append("update_at", System.currentTimeMillis());
        department.append("create_by", request.getInfo().getUsername());
        department.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_DEPARTMENT, department);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateDepartment(UpdateDepartmentRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_DEPARTMENT, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_DEPARTMENT, Filters.eq("name_search", name.toLowerCase()));
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
        db.update(CollectionNameDefs.COLL_DEPARTMENT, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteDepartment(DeleteDepartmentRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_DEPARTMENT, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_DEPARTMENT, cond);
        return new BaseResponse(0, "OK");
    }
}
