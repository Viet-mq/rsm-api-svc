package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.BlacklistEntity;
import com.edso.resume.api.domain.request.CreateBlacklistRequest;
import com.edso.resume.api.domain.request.DeleteBlacklistRequest;
import com.edso.resume.api.domain.request.UpdateBlacklistRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ErrorCodeDefs;
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

    public BlacklistServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<BlacklistEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_BLACKLIST, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_BLACKLIST, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<BlacklistEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                BlacklistEntity blacklist = BlacklistEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .email(AppUtils.parseString(doc.get("EMAIL")))
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

        try {
            String name = request.getName();
            response = check(request.getEmail(), request.getPhoneNumber(), request.getSsn());
            if (response != null) return response;

            Document blacklist = new Document();
            blacklist.append("id", UUID.randomUUID().toString());
            blacklist.append("name", name);
            blacklist.append("EMAIL", request.getEmail().replaceAll(" ", ""));
            blacklist.append("phoneNumber", request.getPhoneNumber().replaceAll(" ", ""));
            blacklist.append("ssn", request.getSsn());
            blacklist.append("reason", request.getReason());
            blacklist.append("name_search", name.toLowerCase());
            blacklist.append("create_at", System.currentTimeMillis());
            blacklist.append("update_at", System.currentTimeMillis());
            blacklist.append("create_by", request.getInfo().getUsername());
            blacklist.append("update_by", request.getInfo().getUsername());

            db.insertOne(CollectionNameDefs.COLL_BLACKLIST, blacklist);

        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateBlacklist(UpdateBlacklistRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq("id", id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, cond);

            if (idDocument == null) {
                response.setResult(ErrorCodeDefs.ID, "Id này không tồn tại");
                return response;
            }

            Bson updates = Updates.combine(
                    Updates.set("EMAIL", request.getEmail().replaceAll(" ", "")),
                    Updates.set("phoneNumber", request.getPhoneNumber().replaceAll(" ", "")),
                    Updates.set("ssn", request.getSsn().replaceAll(" ", "")),
                    Updates.set("reason", request.getReason()),
                    Updates.set("name", request.getName()),
                    Updates.set("name_search", AppUtils.parseVietnameseToEnglish(request.getName())),
                    Updates.set("update_at", System.currentTimeMillis()),
                    Updates.set("update_by", request.getInfo().getUsername()),
                    Updates.set("update_blacklist_at", System.currentTimeMillis()),
                    Updates.set("update_blacklist_by", request.getInfo().getUsername())
            );

            db.update(CollectionNameDefs.COLL_BLACKLIST, cond, updates, true);
            response.setSuccess();
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        return response;
    }

    @Override
    public BaseResponse deleteBlacklist(DeleteBlacklistRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq("id", id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_BLACKLIST, cond);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse check(String email, String phoneNumber, String ssn) {
        BaseResponse response = new BaseResponse();
        Bson emailCond = Filters.eq("EMAIL", email.replaceAll(" ", ""));
        Bson phoneCond = Filters.eq("phoneNumber", phoneNumber.replaceAll(" ", ""));
        Bson ssnCond = Filters.eq("ssn", ssn.replaceAll(" ", ""));
        Document emailDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, emailCond);
        Document phoneDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, phoneCond);
        Document ssDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, ssnCond);

        if (emailDocument != null) {
            response.setFailed("email đã tồn tại");
            return response;
        }
        if (phoneDocument != null) {
            response.setFailed("Số điện thoại đã tồn tại");
            return response;
        }
        if (ssDocument != null) {
            response.setFailed("Số CMND đã tồn tại");
            return response;
        }

        return new BaseResponse(0, "OK");
    }

    @Override
    public Boolean checkBlacklist(String email, String phoneNumber, String ssn) {
        Bson emailCond = Filters.eq("EMAIL", email.replaceAll(" ", ""));
        Bson phoneCond = Filters.eq("phoneNumber", phoneNumber.replaceAll(" ", ""));
        Bson ssnCond = Filters.eq("ssn", ssn.replaceAll(" ", ""));
        Document emailDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, emailCond);
        Document phoneDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, phoneCond);
        Document ssDocument = db.findOne(CollectionNameDefs.COLL_BLACKLIST, ssnCond);

        return emailDocument == null && phoneDocument == null && ssDocument == null;
    }
}
