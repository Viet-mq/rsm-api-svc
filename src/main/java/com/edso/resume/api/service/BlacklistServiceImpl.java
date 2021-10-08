package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.BlacklistEntity;
import com.edso.resume.api.domain.request.CreateBlacklistRequest;
import com.edso.resume.api.domain.request.DeleteBlacklistRequest;
import com.edso.resume.api.domain.request.UpdateBlacklistRequest;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class BlacklistServiceImpl extends BaseService implements BlacklistService {

    private final MongoDbOnlineSyncActions db;

    public  BlacklistServiceImpl (MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate){
        super(db, rabbitTemplate);
        this.db = db;
    }

    @Override
    public GetArrayResponse<BlacklistEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if(!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_BLACKLIST, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_BLACKLIST, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<BlacklistEntity> rows = new ArrayList<>();
        if(lst != null){
            for (Document doc : lst){
                BlacklistEntity blacklist =  BlacklistEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                        .phoneNumber(AppUtils.parseString(doc.get(DbKeyConfig.PHONE_NUMBER)))
                        .SSN(AppUtils.parseString(doc.get(DbKeyConfig.SSN)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .reason(AppUtils.parseString(doc.get(DbKeyConfig.REASON)))
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
        Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
        long count = db.countAll(CollectionNameDefs.COLL_BLACKLIST, c);

        if (count > 0) {
            response.setFailed("Name already existed !");
            return response;
        }

        Document blacklist = new Document();
        blacklist.append(DbKeyConfig.ID, UUID.randomUUID().toString());
        blacklist.append(DbKeyConfig.NAME, name);
        blacklist.append(DbKeyConfig.EMAIL, request.getEmail());
        blacklist.append(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber());
        blacklist.append(DbKeyConfig.SSN, request.getSSN());
        blacklist.append(DbKeyConfig.REASON, request.getReason());
        blacklist.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
        blacklist.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
        blacklist.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
        blacklist.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
        blacklist.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

        db.insertOne(CollectionNameDefs.COLL_BLACKLIST, blacklist);

        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse updateBlacklist(UpdateBlacklistRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String name = request.getName();
        Document obj = db.findOne(CollectionNameDefs.COLL_BLACKLIST, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
        if (obj != null) {
            String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
            if (!objId.equals(id)) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }
        }

        //update roles
        Bson updates = Updates.combine(
                Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                Updates.set(DbKeyConfig.SSN, request.getSSN()),
                Updates.set(DbKeyConfig.REASON, request.getReason()),
                Updates.set(DbKeyConfig.NAME, request.getName()),
                Updates.set(DbKeyConfig.NAME_SEARCH, request.getName().toLowerCase()),
                Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                Updates.set(DbKeyConfig.UPDATE_BLACKLIST_AT, System.currentTimeMillis()),
                Updates.set(DbKeyConfig.UPDATE_BLACKLIST_BY, request.getInfo().getUsername())
        );

        db.update(CollectionNameDefs.COLL_BLACKLIST, cond, updates, true);
        response.setSuccess();

        return response;
    }

    @Override
    public BaseResponse deleteBlacklist(DeleteBlacklistRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }
        db.delete(CollectionNameDefs.COLL_BLACKLIST, cond);
        return new BaseResponse(0, "OK");
    }
}
