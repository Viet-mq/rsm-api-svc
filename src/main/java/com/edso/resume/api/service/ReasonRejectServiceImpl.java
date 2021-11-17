package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ReasonRejectEntity;
import com.edso.resume.api.domain.request.CreateReasonRejectRequest;
import com.edso.resume.api.domain.request.DeleteReasonRejectRequest;
import com.edso.resume.api.domain.request.UpdateReasonRejectRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
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
public class ReasonRejectServiceImpl extends BaseService implements ReasonRejectService {

    protected ReasonRejectServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }


    @Override
    public GetArrayResponse<ReasonRejectEntity> findAll(HeaderInfo info, Integer page, Integer size) {
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_REASON_REJECT, null, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ReasonRejectEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ReasonRejectEntity reason = ReasonRejectEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .reason(AppUtils.parseString(doc.get(DbKeyConfig.REASON)))
                        .build();
                rows.add(reason);
            }
        }
        GetArrayResponse<ReasonRejectEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_REASON_REJECT, null));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createReasonReject(CreateReasonRejectRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document reason = new Document();
            reason.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            reason.append(DbKeyConfig.REASON, request.getReason());
            reason.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            reason.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            reason.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            reason.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_REASON_REJECT, reason);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse updateReasonReject(UpdateReasonRejectRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_REASON_REJECT, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.REASON, request.getReason()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_REASON_REJECT, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteReasonReject(DeleteReasonRejectRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_REASON_REJECT, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_REASON_REJECT, cond);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        return new BaseResponse(0, "OK");
    }
}
