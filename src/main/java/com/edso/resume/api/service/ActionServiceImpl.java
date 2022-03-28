package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.request.CreateActionRequest;
import com.edso.resume.api.domain.request.DeleteActionRequest;
import com.edso.resume.api.domain.request.UpdateActionRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ActionServiceImpl extends BaseService implements ActionService {
    protected ActionServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public BaseResponse createAction(CreateActionRequest request) {
        BaseResponse response = new BaseResponse();
        try {

            Bson permissionId = Filters.eq(DbKeyConfig.ID, request.getPermissionId());
            Document permission = db.findOne(CollectionNameDefs.COLL_PERMISSION, permissionId);
            if (permission == null) {
                response.setFailed("Không tồn tại permission này");
                return response;
            }

            List<Document> actions = (List<Document>) permission.get(DbKeyConfig.ACTIONS);
            for (Document document : actions) {
                if (AppUtils.parseString(document.get(DbKeyConfig.KEY)).equalsIgnoreCase(request.getKey())) {
                    response.setFailed("Key này đã tồn tại");
                    return response;
                }
            }

            Document action = new Document();
            action.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            action.append(DbKeyConfig.TITLE, AppUtils.mergeWhitespace(request.getTitle()));
            action.append(DbKeyConfig.KEY, request.getKey());

            Bson pushAction = Updates.push(DbKeyConfig.ACTIONS, action);

            db.update(CollectionNameDefs.COLL_PERMISSION, permissionId, pushAction);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse updateAction(UpdateActionRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Bson permissionId = Filters.eq(DbKeyConfig.ID, request.getPermissionId());
            Document permission = db.findOne(CollectionNameDefs.COLL_PERMISSION, permissionId);
            if (permission == null) {
                response.setFailed("Không tồn tại permission này");
                return response;
            }

            List<Document> actions = (List<Document>) permission.get(DbKeyConfig.ACTIONS);
            for (Document document : actions) {
                if (AppUtils.parseString(document.get(DbKeyConfig.KEY)).equalsIgnoreCase(request.getKey()) && !AppUtils.parseString(document.get(DbKeyConfig.ID)).equals(request.getId())) {
                    response.setFailed("Key này đã tồn tại");
                    return response;
                }
            }

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.ACTIONS_TITLE, request.getTitle()),
                    Updates.set(DbKeyConfig.ACTIONS_KEY, request.getKey())
            );

            db.update(CollectionNameDefs.COLL_PERMISSION, Filters.and(permissionId, Filters.eq(DbKeyConfig.ACTIONS_ID, request.getId())), updates);

            Bson updateViewRole = Updates.combine(
                    Updates.set("permissions.$[].actions.$[act].title", request.getTitle()),
                    Updates.set("permissions.$[].actions.$[act].key", request.getKey())
            );

            UpdateOptions options = new UpdateOptions().arrayFilters(Collections.singletonList(Filters.eq("act.id", request.getId())));
            db.update(CollectionNameDefs.COLL_VIEW_ROLE, Filters.and(Filters.eq("permissions.id", request.getPermissionId()), Filters.eq("permissions.actions.id", request.getId())), updateViewRole, options);

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteAction(DeleteActionRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            long count = db.countAll(CollectionNameDefs.COLL_VIEW_ROLE, Filters.eq("permissions.actions.id", request.getId()));
            if (count > 0) {
                response.setFailed("Không thể xóa action này");
                return response;
            }

            Bson permissionId = Filters.eq(DbKeyConfig.ID, request.getPermissionId());
            Document permission = db.findOne(CollectionNameDefs.COLL_PERMISSION, permissionId);
            if (permission == null) {
                response.setFailed("Không tồn tại permission này");
                return response;
            }

            boolean check = false;
            List<Document> actions = (List<Document>) permission.get(DbKeyConfig.ACTIONS);
            for (Document document : actions) {
                if (AppUtils.parseString(document.get(DbKeyConfig.ID)).equals(request.getId())) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            Bson action = Updates.pull(DbKeyConfig.ACTIONS, Filters.eq(DbKeyConfig.ID, request.getId()));

            db.update(CollectionNameDefs.COLL_PERMISSION, permissionId, action);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
