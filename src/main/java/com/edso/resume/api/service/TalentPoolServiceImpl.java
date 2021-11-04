package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.api.domain.request.CreateTalentPoolRequest;
import com.edso.resume.api.domain.request.DeleteTalentPoolRequest;
import com.edso.resume.api.domain.request.UpdateTalentPoolRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.github.slugify.Slugify;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class TalentPoolServiceImpl extends BaseService implements TalentPoolService {

    public TalentPoolServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<TalentPoolEntity> findAll(HeaderInfo headerInfo, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_TALENT_POOL, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<TalentPoolEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                TalentPoolEntity talentPool = TalentPoolEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .managers(parseList(doc.get("managers")))
                        .description(AppUtils.parseString(doc.get("description")))
                        .numberOfProfile(AppUtils.parseInt(doc.get("numberOfProfile")))
                        .build();
                rows.add(talentPool);
            }
        }
        GetArrayResponse<TalentPoolEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(rows.size());
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createTalentPool(CreateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_TALENT_POOL, c);

        if (count > 0) {
            response.setFailed("Tên Talent Pool đã tồn tại!");
            return response;
        }

        //Check if manager is already in the system or not
        List<String> managers = request.getManagers();
        for (String manager : managers) {
            Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq("username", manager));
            if (user == null) {
                response.setFailed("user " + manager + " không tồn tại");
                return response;
            }
        }

        Slugify slugify = new Slugify();
        Document talentPool = new Document();
        talentPool.append("id", slugify.slugify(name) + new Random().nextInt(10000));
        talentPool.append("name", name);
        talentPool.append("managers", request.getManagers());
        talentPool.append("description", request.getDescription());
        talentPool.append("numberOfProfile", 0);
        talentPool.append("name_search", name.toLowerCase());
        talentPool.append("create_at", System.currentTimeMillis());
        talentPool.append("update_at", System.currentTimeMillis());
        talentPool.append("create_by", request.getInfo().getUsername());
        talentPool.append("update_by", request.getInfo().getUsername());

        db.insertOne(CollectionNameDefs.COLL_TALENT_POOL, talentPool);

        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateTalentPool(UpdateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        //Check if user have permission to update Talent Pool
        List<String> managers = parseList(idDocument.get("managers"));
        int check = 0;
        for (String manager : managers)
            if (manager.equals(request.getInfo().getUsername())) check = 1;
        if (check == 0) {
            response.setFailed("Người dùng không có quyền sửa Talent Pool");
            return response;
        }

        //Check if the name already exists or not
        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, Filters.eq("name_search", name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get("id"));
            if (!objId.equals(id)) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }
        }

        //Check if manager is already in the system or not
        managers = request.getManagers();
        for (String manager : managers) {
            Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq("username", manager));
            if (user == null) {
                response.setFailed("user " + manager + " không tồn tại");
                return response;
            }
        }

        //update
        Bson updates = Updates.combine(
                Updates.set("name", name),
                Updates.set("name_search", name.toLowerCase()),
                Updates.set("managers", request.getManagers()),
                Updates.set("description", request.getDescription()),
                Updates.set("numberOfProfile", request.getNumberOfProfile()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_TALENT_POOL, cond, updates, true);

        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteTalentPool(DeleteTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        //Check if user have permission to delete Talent Pool
        List<String> managers = parseList(idDocument.get("managers"));
        int check = 0;
        for (String manager : managers) {
            if (manager.equals(request.getInfo().getUsername())) check = 1;
        }

        if (check == 0) {
            response.setFailed("Người dùng không có quyền xóa Talent Pool");
            return response;
        }

        //delete
        db.delete(CollectionNameDefs.COLL_TALENT_POOL, cond);
        return new BaseResponse(0, "OK");
    }
}
