package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Permission;
import com.edso.resume.api.domain.entities.ViewRoleEntity;
import com.edso.resume.api.domain.request.CreateViewRoleRequest;
import com.edso.resume.api.domain.request.DeleteViewRoleRequest;
import com.edso.resume.api.domain.request.UpdateViewRoleRequest;
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
public class ViewRoleServiceImpl extends BaseService implements ViewRoleService {
    protected ViewRoleServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<ViewRoleEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        if (!Strings.isNullOrEmpty(id)) {
            c.add(Filters.eq(DbKeyConfig.ID, id));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_VIEW_ROLE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ViewRoleEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ViewRoleEntity viewRole = ViewRoleEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .description(AppUtils.parseString(doc.get(DbKeyConfig.DESCRIPTION)))
                        .permissions((List<Permission>) doc.get(DbKeyConfig.PERMISSIONS))
                        .build();
                rows.add(viewRole);
            }
        }
        GetArrayResponse<ViewRoleEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_VIEW_ROLE, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createViewRole(CreateViewRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {

            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_VIEW_ROLE, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            List<Document> permissions = new ArrayList<>();
            for (Permission p : request.getPermissions()) {
                Document permissionDoc = db.findOne(CollectionNameDefs.COLL_PERMISSION, Filters.eq(DbKeyConfig.ID, p.getPermissionId()));
                if (permissionDoc == null) {
                    response.setFailed("Không tồn tại permission này");
                    return response;
                }
                List<Document> actions = (List<Document>) permissionDoc.get(DbKeyConfig.ACTIONS);
                List<Document> actionDoc = new ArrayList<>();
                if (p.getActions() != null && !p.getActions().isEmpty()) {
                    for (Document act : actions) {
                        for (String a : p.getActions()) {
                            if (AppUtils.parseString(act.get(DbKeyConfig.ID)).equals(a)) {
                                actionDoc.add(act);
                            }
                        }
                    }
                }
                Document permission = new Document();
                permission.append(DbKeyConfig.ID, permissionDoc.get(DbKeyConfig.ID));
                permission.append(DbKeyConfig.TITLE, permissionDoc.get(DbKeyConfig.TITLE));
                permission.append(DbKeyConfig.ICON, permissionDoc.get(DbKeyConfig.ICON));
                permission.append(DbKeyConfig.PATH, permissionDoc.get(DbKeyConfig.PATH));
                permission.append(DbKeyConfig.INDEX, permissionDoc.get(DbKeyConfig.INDEX));
                permission.append(DbKeyConfig.ACTIONS, actionDoc);
                permissions.add(permission);
            }

            String id = UUID.randomUUID().toString();
            Document viewRole = new Document();
            viewRole.append(DbKeyConfig.ID, id);
            viewRole.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            viewRole.append(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription()));
            viewRole.append(DbKeyConfig.PERMISSIONS, permissions);
            viewRole.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            viewRole.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            viewRole.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            viewRole.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            db.insertOne(CollectionNameDefs.COLL_VIEW_ROLE, viewRole);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse updateViewRole(UpdateViewRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_VIEW_ROLE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_VIEW_ROLE, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            List<Document> permissions = new ArrayList<>();
            for (Permission p : request.getPermissions()) {
                Document permissionDoc = db.findOne(CollectionNameDefs.COLL_PERMISSION, Filters.eq(DbKeyConfig.ID, p.getPermissionId()));
                if (permissionDoc == null) {
                    response.setFailed("Không tồn tại permission này");
                    return response;
                }
                List<Document> actions = (List<Document>) permissionDoc.get(DbKeyConfig.ACTIONS);
                List<Document> actionDoc = new ArrayList<>();
                if (p.getActions() != null && !p.getActions().isEmpty()) {
                    for (Document act : actions) {
                        for (String a : p.getActions()) {
                            if (AppUtils.parseString(act.get(DbKeyConfig.ID)).equals(a)) {
                                actionDoc.add(act);
                            }
                        }
                    }
                }
                Document permission = new Document();
                permission.append(DbKeyConfig.ID, AppUtils.parseString(permissionDoc.get(DbKeyConfig.ID)));
                permission.append(DbKeyConfig.TITLE, AppUtils.parseString(permissionDoc.get(DbKeyConfig.TITLE)));
                permission.append(DbKeyConfig.ICON, AppUtils.parseString(permissionDoc.get(DbKeyConfig.ICON)));
                permission.append(DbKeyConfig.PATH, AppUtils.parseString(permissionDoc.get(DbKeyConfig.PATH)));
                permission.append(DbKeyConfig.INDEX, AppUtils.parseLong(permissionDoc.get(DbKeyConfig.INDEX)));
                permission.append(DbKeyConfig.ACTIONS, actionDoc);
                permissions.add(permission);
            }

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.DESCRIPTION, request.getDescription()),
                    Updates.set(DbKeyConfig.PERMISSIONS, permissions),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_VIEW_ROLE, cond, updates);

            Bson updateRole = Updates.combine(
                    Updates.set("view_roles.$.name", AppUtils.mergeWhitespace(name)),
                    Updates.set("view_roles.$.description", request.getDescription())
            );
            db.update(CollectionNameDefs.COLL_ROLE, Filters.eq("view_roles.id"), updateRole);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }

    }

    @Override
    public BaseResponse deleteViewRole(DeleteViewRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            long count = db.countAll(CollectionNameDefs.COLL_ROLE, Filters.eq("view_roles.id", id));
            if (count > 0) {
                response.setFailed("Không thể xóa view role này");
                return response;
            }

            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_VIEW_ROLE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_VIEW_ROLE, cond);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
