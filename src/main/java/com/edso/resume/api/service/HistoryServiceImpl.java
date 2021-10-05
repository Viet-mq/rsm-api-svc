package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.request.CreateHistoryRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class HistoryServiceImpl extends BaseService implements HistoryService {

    private final MongoDbOnlineSyncActions db;

    public HistoryServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_HISTORY_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_HISTORY_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<HistoryEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                HistoryEntity history = HistoryEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .time(AppUtils.parseLong(doc.get("time")))
                        .action(AppUtils.parseString(doc.get("action")))
                        .by(AppUtils.parseString(doc.get("by")))
                        .build();
                rows.add(history);
            }
        }
        GetArrayResponse<HistoryEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createHistory(CreateHistoryRequest request) {

        BaseResponse response = new BaseResponse();

        Document profile = new Document();
        profile.append("id", UUID.randomUUID().toString());
        profile.append("idProfile", request.getIdProfile());
        profile.append("time", request.getTime());
        profile.append("action", request.getAction());
        profile.append("by", request.getBy());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_HISTORY_PROFILE, profile);

        response.setSuccess();
        return response;
    }
}
