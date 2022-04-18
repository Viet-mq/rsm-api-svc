package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ApiRoleEntity;
import com.edso.resume.api.domain.request.CreateApiRoleRequest;
import com.edso.resume.api.domain.request.DeleteApiRoleRequest;
import com.edso.resume.api.domain.request.UpdateApiRoleRequest;
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
public class ApiRoleServiceImpl extends BaseService implements ApiRoleService {

    protected ApiRoleServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<ApiRoleEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size) {
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
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_API_ROLE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ApiRoleEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ApiRoleEntity role = ApiRoleEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .description(AppUtils.parseString(doc.get(DbKeyConfig.DESCRIPTION)))
                        .apis((List<String>) doc.get(DbKeyConfig.APIS))
                        .build();
                rows.add(role);
            }
        }
        GetArrayResponse<ApiRoleEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_API_ROLE, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createApiRole(CreateApiRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {

            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_API_ROLE, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            List<Document> apis = db.findAll(CollectionNameDefs.COLL_API, Filters.in(DbKeyConfig.ID, request.getApis()), null, 0, 0);
            if (apis.size() != request.getApis().size()) {
                response.setFailed("Không tồn tại api này");
                return response;
            }

            String id = UUID.randomUUID().toString();
            Document role = new Document();
            role.append(DbKeyConfig.ID, id);
            role.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            role.append(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription()));
            role.append(DbKeyConfig.APIS, request.getApis());
            role.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            role.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            role.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            role.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            db.insertOne(CollectionNameDefs.COLL_API_ROLE, role);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse updateApiRole(UpdateApiRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_API_ROLE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_API_ROLE, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            List<Document> apis = db.findAll(CollectionNameDefs.COLL_API, Filters.in(DbKeyConfig.ID, request.getApis()), null, 0, 0);
            if (apis.size() != request.getApis().size()) {
                response.setFailed("Không tồn tại api này");
                return response;
            }

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription())),
                    Updates.set(DbKeyConfig.APIS, request.getApis()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_API_ROLE, cond, updates);

            Bson updateRole = Updates.combine(
                    Updates.set("api_roles.$.name", AppUtils.mergeWhitespace(name)),
                    Updates.set("api_roles.$.description", AppUtils.mergeWhitespace(request.getDescription()))
            );
            db.update(CollectionNameDefs.COLL_ROLE, Filters.eq("api_roles.id"), updateRole);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }

    }

    @Override
    public BaseResponse deleteApiRole(DeleteApiRoleRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();

            long count = db.countAll(CollectionNameDefs.COLL_ROLE, Filters.eq("api_roles.id", id));
            if (count > 0) {
                response.setFailed("Không thể xóa api role này");
                return response;
            }

            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_API_ROLE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_API_ROLE, cond);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
