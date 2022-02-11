package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ReminderEntity;
import com.edso.resume.api.domain.request.CreateReminderRequest;
import com.edso.resume.api.domain.request.DeleteReminderRequest;
import com.edso.resume.api.domain.request.UpdateReminderRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReminderServiceImpl extends BaseService implements ReminderService {
    protected ReminderServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<ReminderEntity> findAll(HeaderInfo info, Long from, Long to) {
        List<Bson> c = new ArrayList<>();
        if (from != null && from > 0) {
            c.add(Filters.gte(DbKeyConfig.TIME, from));
        }
        if (to != null && to > 0) {
            c.add(Filters.lte(DbKeyConfig.TIME, to));
        }
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_REMINDER, cond, null, 0, 0);
        List<ReminderEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ReminderEntity reminder = ReminderEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .content(AppUtils.parseString(doc.get(DbKeyConfig.CONTENT)))
                        .repeat(AppUtils.parseString(doc.get(DbKeyConfig.REPEAT)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .createAt(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_AT)))
                        .createBy(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_BY)))
                        .updateAt(AppUtils.parseString(doc.get(DbKeyConfig.UPDATE_AT)))
                        .updateBy(AppUtils.parseString(doc.get(DbKeyConfig.UPDATE_BY)))
                        .build();
                rows.add(reminder);
            }
        }
        GetArrayResponse<ReminderEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_REMINDER, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createReminder(CreateReminderRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document reminder = new Document();
            reminder.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            reminder.append(DbKeyConfig.REPEAT, request.getRepeat());
            reminder.append(DbKeyConfig.CONTENT, AppUtils.mergeWhitespace(request.getContent()));
            reminder.append(DbKeyConfig.TIME, request.getTime());
            reminder.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            reminder.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_REMINDER, reminder);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;
        }
    }

    @Override
    public BaseResponse updateReminder(UpdateReminderRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document reminder = db.findOne(CollectionNameDefs.COLL_REMINDER, Filters.eq(DbKeyConfig.ID, request.getId()));

            if (reminder == null) {
                response.setFailed("Id reminder này không tồn tại!");
                return response;
            }

            Bson update = Updates.combine(
                    Updates.set(DbKeyConfig.REPEAT, request.getRepeat()),
                    Updates.set(DbKeyConfig.CONTENT, AppUtils.mergeWhitespace(request.getContent())),
                    Updates.set(DbKeyConfig.TIME, request.getTime()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );

            // insert to database
            db.update(CollectionNameDefs.COLL_REMINDER, Filters.eq(DbKeyConfig.ID, request.getId()), update);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse deleteReminder(DeleteReminderRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_REMINDER, cond);

            if (idDocument == null) {
                response.setFailed("Id reminder này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_REMINDER, cond);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
