package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.BlacklistEntity;
import com.edso.resume.api.domain.request.CreateBlacklistRequest;
import com.edso.resume.api.domain.request.DeleteBlacklistRequest;
import com.edso.resume.api.domain.request.UpdateBlacklistRequest;
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
public class BlacklistServiceImpl extends BaseService implements BlacklistService {

    private final MongoDbOnlineSyncActions db;

    public  BlacklistServiceImpl (MongoDbOnlineSyncActions db){
        this.db = db;
    }

    @Override
    public GetArrayResponse<BlacklistEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if(!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_BLACKLIST, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_BLACKLIST, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<BlacklistEntity> rows = new ArrayList<>();
        if(lst != null){
            for (Document doc : lst){
                BlacklistEntity blacklist =  BlacklistEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .ssn(AppUtils.parseString(doc.get("ssn")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .reason(AppUtils.parseString(doc.get("reason")))
                        .build();
                rows.add(blacklist);
            }
        }

        GetArrayResponse<BlacklistEntity> response = new GetArrayResponse<>();
        response.setSuccess();
        response.setTotal(total);
        response.setRows(rows);

        return response;
    }

    @Override
    public BaseResponse createBlacklist(CreateBlacklistRequest request) {

        BaseResponse response = new BaseResponse();

        String name = request.getName();
        Bson c = Filters.eq("name_search", name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_BLACKLIST, c);

        if (count > 0) {
            response.setFailed("Name already existed !");
            return response;
        }

        Document blacklist = new Document();
        blacklist.append("id", UUID.randomUUID().toString());
        blacklist.append("name", name);
        blacklist.append("email", request.getEmail());
        blacklist.append("phoneNumber", request.getPhoneNumber());
        blacklist.append("ssn", request.getSsn());
        blacklist.append("reason", request.getReason());
        blacklist.append("name_search", name.toLowerCase());
        blacklist.append("create_at", System.currentTimeMillis());
        blacklist.append("update_at", System.currentTimeMillis());
        blacklist.append("create_by", request.getInfo().getUsername());
        blacklist.append("update_by", request.getInfo().getUsername());

        db.insertOne(CollectionNameDefs.COLL_BLACKLIST, blacklist);

        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse updateBlacklist(UpdateBlacklistRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_BLACKLIST, Filters.eq("name_search", name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get("id"));
            if (!objId.equals(id)) {
                response.setFailed("Name already existed !");
                return response;
            }
        }

        //update roles
        String reason = AppUtils.parseString(idDocument.get("reason"));
        logger.info(reason);
        if(request.getReason() != null) reason = request.getReason();
        Bson updates = Updates.combine(
                Updates.set("email", request.getEmail()),
                Updates.set("phoneNumber", request.getPhoneNumber()),
                Updates.set("ssn", request.getSsn()),
                Updates.set("reason", reason),
                Updates.set("name", request.getName()),
                Updates.set("name_search", request.getName().toLowerCase()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername()),
                Updates.set("update_blacklist_at", System.currentTimeMillis()),
                Updates.set("update_blacklist_by", request.getInfo().getUsername())
        );

        db.update(CollectionNameDefs.COLL_BLACKLIST, cond, updates, true);
        response.setSuccess();

        return response;
    }

    @Override
    public BaseResponse deleteBlacklist(DeleteBlacklistRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }
        db.delete(CollectionNameDefs.COLL_BLACKLIST, cond);
        return new BaseResponse(0, "OK");
    }
}
