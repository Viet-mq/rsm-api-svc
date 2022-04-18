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
public class ReasonRejectServiceImpl extends BaseService implements ReasonRejectService {

    protected ReasonRejectServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }


    @Override
    public GetArrayResponse<ReasonRejectEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_REASON_REJECT, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
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
            String name = request.getReason();
            Bson c = Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            long count = db.countAll(CollectionNameDefs.COLL_JOB_LEVEL, c);

            if (count > 0) {
                response.setFailed("Lý do này đã tồn tại");
                return response;
            }

            Document reason = new Document();
            reason.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            reason.append(DbKeyConfig.REASON, AppUtils.mergeWhitespace(request.getReason()));
            reason.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(request.getReason().toLowerCase()));
            reason.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getReason()));
            reason.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            reason.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            reason.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            reason.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());
            reason.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getMyOrganizations());

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

            String name = request.getReason();
            Document obj = db.findOne(CollectionNameDefs.COLL_JOB, Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Lý do này đã tồn tại");
                    return response;
                }
            }

            Bson updateReject = Updates.combine(
                    Updates.set(DbKeyConfig.REASON, AppUtils.mergeWhitespace(request.getReason()))
            );
            db.update(CollectionNameDefs.COLL_REASON_REJECT_PROFILE, Filters.eq(DbKeyConfig.REASON_ID, request.getId()), updateReject);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.REASON, AppUtils.mergeWhitespace(request.getReason())),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getReason())),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(request.getReason().toLowerCase())),
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
            Document reason = db.findOne(CollectionNameDefs.COLL_REASON_REJECT_PROFILE, Filters.eq(DbKeyConfig.REASON_ID, request.getId()));
            if (reason == null) {
                String id = request.getId();
                Bson cond = Filters.eq(DbKeyConfig.ID, id);
                Document idDocument = db.findOne(CollectionNameDefs.COLL_REASON_REJECT, cond);

                if (idDocument == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
                db.delete(CollectionNameDefs.COLL_REASON_REJECT, cond);
                response.setSuccess();
                return response;
            } else {
                response.setFailed("Không thể xóa lý do loại này!");
                return response;
            }
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
