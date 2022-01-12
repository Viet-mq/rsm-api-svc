package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryEmailServiceImpl extends BaseService implements HistoryEmailService {
    protected HistoryEmailServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public void createHistoryEmail(HistoryEmail historyEmail, HeaderInfo info) {
        try {
            Document fullName = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));

            Document history = new Document();
            history.append(DbKeyConfig.ID, historyEmail.getId());
            history.append(DbKeyConfig.ID_PROFILE, historyEmail.getIdProfile());
            history.append(DbKeyConfig.SUBJECT, historyEmail.getSubject());
            history.append(DbKeyConfig.CONTENT, historyEmail.getContent());
            history.append(DbKeyConfig.TIME, System.currentTimeMillis());
            history.append(DbKeyConfig.USERNAME, info.getUsername());
            history.append(DbKeyConfig.FULL_NAME, fullName.get(DbKeyConfig.FULL_NAME));

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_HISTORY_EMAIL, history);
            logger.info("createHistoryEmail history: {}", history);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
    }

    @Override
    public void deleteHistoryEmail(String idProfile) {
        db.delete(CollectionNameDefs.COLL_HISTORY_EMAIL, Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        logger.info("deleteHistoryEmail idProfile: {}", idProfile);
    }

    @Override
    public GetArrayResponse<HistoryEmail> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size) {
        GetArrayResponse<HistoryEmail> resp = new GetArrayResponse<>();

        Bson con = Filters.eq(DbKeyConfig.ID, idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if (idProfileDocument == null) {
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }

        Bson cond = Filters.eq(DbKeyConfig.ID_PROFILE, idProfile);
        Bson sort = Filters.eq(DbKeyConfig.TIME, -1);

        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_HISTORY_PROFILE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<HistoryEmail> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                HistoryEmail history = HistoryEmail.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .build();
                rows.add(history);
            }
        }

        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_HISTORY_EMAIL, cond));
        resp.setRows(rows);
        return resp;
    }
}
