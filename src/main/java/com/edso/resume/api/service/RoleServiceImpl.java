package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.RoleEntity;
import com.edso.resume.api.domain.request.CreateRoleRequest;
import com.edso.resume.api.domain.request.DeleteRoleRequest;
import com.edso.resume.api.domain.request.UpdateRoleRequest;
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
public class RoleServiceImpl extends BaseService implements RoleService {
    protected RoleServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<RoleEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size) {
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
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_ROLE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<RoleEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                RoleEntity role = RoleEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .description(AppUtils.parseString(doc.get(DbKeyConfig.DESCRIPTION)))
                        .viewRoles((List<Document>) doc.get(DbKeyConfig.VIEW_ROLES))
                        .apiRoles((List<Document>) doc.get(DbKeyConfig.API_ROLES))
                        .build();
                rows.add(role);
            }
        }
        GetArrayResponse<RoleEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_ROLE, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createRole(CreateRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {

            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_ROLE, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            FindIterable<Document> roles = db.findAll2(CollectionNameDefs.COLL_VIEW_ROLE, Filters.in(DbKeyConfig.ID, request.getViewRoles()), null, 0, 0);
            List<Document> viewRoleResult = new ArrayList<>();
            if (roles == null) {
                response.setFailed("Không tồn tại view role này");
                return response;
            }
            for (Document document : roles) {
                document.remove("_id");
                document.remove(DbKeyConfig.NAME_SEARCH);
                document.remove(DbKeyConfig.NAME_EQUAL);
                document.remove(DbKeyConfig.CREATE_AT);
                document.remove(DbKeyConfig.CREATE_BY);
                document.remove(DbKeyConfig.UPDATE_AT);
                document.remove(DbKeyConfig.UPDATE_BY);
                document.remove(DbKeyConfig.PERMISSIONS);
                viewRoleResult.add(document);
            }
            if (viewRoleResult.size() != request.getViewRoles().size()) {
                response.setFailed("Không tồn tại view role này");
                return response;
            }

            FindIterable<Document> apiRoles = db.findAll2(CollectionNameDefs.COLL_API_ROLE, Filters.in(DbKeyConfig.ID, request.getApiRoles()), null, 0, 0);
            List<Document> apiRoleResult = new ArrayList<>();
            if (apiRoles == null) {
                response.setFailed("Không tồn tại api role này");
                return response;
            }
            for (Document document : apiRoles) {
                document.remove("_id");
                document.remove(DbKeyConfig.NAME_SEARCH);
                document.remove(DbKeyConfig.NAME_EQUAL);
                document.remove(DbKeyConfig.CREATE_AT);
                document.remove(DbKeyConfig.CREATE_BY);
                document.remove(DbKeyConfig.UPDATE_AT);
                document.remove(DbKeyConfig.UPDATE_BY);
                document.remove(DbKeyConfig.APIS);
                apiRoleResult.add(document);
            }
            if (apiRoleResult.size() != request.getApiRoles().size()) {
                response.setFailed("Không tồn tại api role này");
                return response;
            }

            String id = UUID.randomUUID().toString();
            Document role = new Document();
            role.append(DbKeyConfig.ID, id);
            role.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            role.append(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription()));
            role.append(DbKeyConfig.VIEW_ROLES, viewRoleResult);
            role.append(DbKeyConfig.API_ROLES, apiRoleResult);
            role.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            role.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            role.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            role.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            db.insertOne(CollectionNameDefs.COLL_ROLE, role);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse updateRole(UpdateRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ROLE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_ROLE, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            FindIterable<Document> roles = db.findAll2(CollectionNameDefs.COLL_VIEW_ROLE, Filters.in(DbKeyConfig.ID, request.getViewRoles()), null, 0, 0);
            List<Document> viewRoleResult = new ArrayList<>();
            if (roles == null) {
                response.setFailed("Không tồn tại view role này");
                return response;
            }
            for (Document document : roles) {
                document.remove("_id");
                document.remove(DbKeyConfig.NAME_SEARCH);
                document.remove(DbKeyConfig.NAME_EQUAL);
                document.remove(DbKeyConfig.CREATE_AT);
                document.remove(DbKeyConfig.CREATE_BY);
                document.remove(DbKeyConfig.UPDATE_AT);
                document.remove(DbKeyConfig.UPDATE_BY);
                document.remove(DbKeyConfig.PERMISSIONS);
                viewRoleResult.add(document);
            }
            if (viewRoleResult.size() != request.getViewRoles().size()) {
                response.setFailed("Không tồn tại view role này");
                return response;
            }

            FindIterable<Document> apiRoles = db.findAll2(CollectionNameDefs.COLL_API_ROLE, Filters.in(DbKeyConfig.ID, request.getApiRoles()), null, 0, 0);
            List<Document> apiRoleResult = new ArrayList<>();
            if (apiRoles == null) {
                response.setFailed("Không tồn tại api role này");
                return response;
            }
            for (Document document : apiRoles) {
                document.remove("_id");
                document.remove(DbKeyConfig.NAME_SEARCH);
                document.remove(DbKeyConfig.NAME_EQUAL);
                document.remove(DbKeyConfig.CREATE_AT);
                document.remove(DbKeyConfig.CREATE_BY);
                document.remove(DbKeyConfig.UPDATE_AT);
                document.remove(DbKeyConfig.UPDATE_BY);
                document.remove(DbKeyConfig.APIS);
                apiRoleResult.add(document);
            }
            if (apiRoleResult.size() != request.getApiRoles().size()) {
                response.setFailed("Không tồn tại api role này");
                return response;
            }

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription())),
                    Updates.set(DbKeyConfig.VIEW_ROLES, viewRoleResult),
                    Updates.set(DbKeyConfig.API_ROLES, apiRoleResult),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_ROLE, cond, updates);

            Bson updateUser = Updates.combine(
                    Updates.set("roles.$.name", AppUtils.mergeWhitespace(name)),
                    Updates.set("roles.$.description", AppUtils.mergeWhitespace(request.getDescription()))
            );
            db.update(CollectionNameDefs.COLL_USER, Filters.eq("roles.id", request.getId()), updateUser);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }

    }

    @Override
    public BaseResponse deleteRole(DeleteRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();

            long count = db.countAll(CollectionNameDefs.COLL_USER, Filters.eq("roles.id", id));
            if (count > 0) {
                response.setFailed("Không thể xóa role này");
                return response;
            }

            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ROLE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_ROLE, cond);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
