package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.VillageEntity;
import com.edso.resume.api.domain.request.CreateVillageRequest;
import com.edso.resume.api.domain.request.DeleteVillageRequest;
import com.edso.resume.api.domain.request.UpdateVillageRequest;
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
public class VillageServiceImpl extends BaseService implements VillageService {

    public VillageServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<VillageEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_VILLAGE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<VillageEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                VillageEntity village = VillageEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .build();
                rows.add(village);
            }
        }
        GetArrayResponse<VillageEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(rows.size());
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createVillage(CreateVillageRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName().trim();
        Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_VILLAGE, c);

        if (count > 0) {
            response.setFailed("Tên này đã tồn tại");
            return response;
        }

        Document village = new Document();
        village.append(DbKeyConfig.ID, UUID.randomUUID().toString());
        village.append(DbKeyConfig.NAME, name);
        village.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
        village.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
        village.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
        village.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
        village.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_VILLAGE, village);

        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateVillage(UpdateVillageRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_VILLAGE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName().trim();
        Document obj = db.findOne(CollectionNameDefs.COLL_VILLAGE, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
            if (!objId.equals(id)) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set(DbKeyConfig.NAME, name),
                Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_VILLAGE, cond, updates, true);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteVillage(DeleteVillageRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_VILLAGE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_VILLAGE, cond);
        return new BaseResponse(0, "OK");
    }

}