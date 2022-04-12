package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.HistoryEntity;
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
import java.util.UUID;

@Service
public class HistoryServiceImpl extends BaseService implements HistoryService {

    public HistoryServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size) {

        GetArrayResponse<HistoryEntity> resp = new GetArrayResponse<>();

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
        List<HistoryEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                HistoryEntity history = HistoryEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .action(AppUtils.parseString(doc.get(DbKeyConfig.ACTION)))
                        .type(AppUtils.parseString(doc.get(DbKeyConfig.TYPE)))
                        .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .build();
                rows.add(history);
            }
        }

        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_HISTORY_PROFILE, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public void createHistory(String idProfile, String type, String action, HeaderInfo info) {
        try {
            Document fullName = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));

            Document history = new Document();
            history.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            history.append(DbKeyConfig.ID_PROFILE, idProfile);
            history.append(DbKeyConfig.TYPE, type);
            history.append(DbKeyConfig.TIME, System.currentTimeMillis());
            history.append(DbKeyConfig.ACTION, action);
            history.append(DbKeyConfig.USERNAME, info.getUsername());
            history.append(DbKeyConfig.FULL_NAME, fullName.get(DbKeyConfig.FULL_NAME));
            history.append(DbKeyConfig.ORGANIZATIONS, info.getOrganizations());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_HISTORY_PROFILE, history);
            logger.info("createHistoryProfile history: {}", history);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
    }


    @Override
    public void deleteHistory(String idProfile) {
        db.delete(CollectionNameDefs.COLL_HISTORY_PROFILE, Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        logger.info("deleteHistoryProfile idProfile: {}", idProfile);
    }

}
