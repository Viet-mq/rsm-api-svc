package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.PermissionEntity;
import com.edso.resume.api.domain.request.CreatePermissionRequest;
import com.edso.resume.api.domain.request.DeletePermissionRequest;
import com.edso.resume.api.domain.request.UpdatePermissionRequest;
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
public class PermissionServiceImpl extends BaseService implements PermissionService {

    protected PermissionServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<PermissionEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        if (!Strings.isNullOrEmpty(id)) {
            c.add(Filters.eq(DbKeyConfig.ID, id));
        }
        Bson sort = Filters.eq(DbKeyConfig.INDEX, 1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PERMISSION, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<PermissionEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                PermissionEntity permission = PermissionEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .title(AppUtils.parseString(doc.get(DbKeyConfig.TITLE)))
                        .path(AppUtils.parseString(doc.get(DbKeyConfig.PATH)))
                        .icon(AppUtils.parseString(doc.get(DbKeyConfig.ICON)))
                        .index(AppUtils.parseLong(doc.get(DbKeyConfig.INDEX)))
                        .actions((List<Document>) doc.get(DbKeyConfig.ACTIONS))
                        .build();
                rows.add(permission);
            }
        }
        GetArrayResponse<PermissionEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_PERMISSION, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createPermission(CreatePermissionRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String path = request.getPath();
            Bson c = Filters.eq(DbKeyConfig.PATH, path.replaceAll(" ", ""));
            long count = db.countAll(CollectionNameDefs.COLL_PERMISSION, c);

            if (count > 0) {
                response.setFailed("Path này đã tồn tại");
                return response;
            }

            Document permission = new Document();
            permission.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            permission.append(DbKeyConfig.TITLE, AppUtils.mergeWhitespace(request.getTitle()));
            permission.append(DbKeyConfig.ICON, request.getIcon());
            permission.append(DbKeyConfig.PATH, path.replaceAll(" ", ""));
            permission.append(DbKeyConfig.INDEX, request.getIndex());
            permission.append(DbKeyConfig.ACTIONS, new ArrayList<>());
            permission.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getTitle()));
            permission.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            permission.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_PERMISSION, permission);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse updatePermission(UpdatePermissionRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_PERMISSION, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String path = request.getPath();
            Document obj = db.findOne(CollectionNameDefs.COLL_PERMISSION, Filters.eq(DbKeyConfig.PATH, path.replaceAll(" ", "")));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Path này đã tồn tại");
                    return response;
                }
            }

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.INDEX, request.getIndex()),
                    Updates.set(DbKeyConfig.TITLE, AppUtils.mergeWhitespace(request.getTitle())),
                    Updates.set(DbKeyConfig.ICON, request.getIcon()),
                    Updates.set(DbKeyConfig.PATH, path.replaceAll(" ", "")),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getTitle())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PERMISSION, cond, updates);

            Bson updateViewRole = Updates.combine(
                    Updates.set(DbKeyConfig.PERMISSIONS_INDEX, request.getIndex()),
                    Updates.set(DbKeyConfig.PERMISSIONS_TITLE, AppUtils.mergeWhitespace(request.getTitle())),
                    Updates.set(DbKeyConfig.PERMISSIONS_ICON, request.getIcon()),
                    Updates.set(DbKeyConfig.PERMISSIONS_PATH, path.replaceAll(" ", ""))
            );
            db.update(CollectionNameDefs.COLL_VIEW_ROLE, Filters.eq(DbKeyConfig.PERMISSIONS_ID, request.getId()), updateViewRole);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }

    }

    @Override
    public BaseResponse deletePermission(DeletePermissionRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();

            long count = db.countAll(CollectionNameDefs.COLL_VIEW_ROLE, Filters.eq(DbKeyConfig.PERMISSIONS_ID, id));
            if (count > 0) {
                response.setFailed("Không thể xóa permission này");
                return response;
            }

            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_PERMISSION, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_PERMISSION, cond);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
