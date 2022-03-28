package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ApiEntity;
import com.edso.resume.api.domain.request.CreateApiRequest;
import com.edso.resume.api.domain.request.DeleteApiRequest;
import com.edso.resume.api.domain.request.UpdateApiRequest;
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
public class ApiServiceImpl extends BaseService implements ApiService {

    protected ApiServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<ApiEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size) {
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
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_API, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ApiEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ApiEntity action = ApiEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .method(AppUtils.parseString(doc.get(DbKeyConfig.METHOD)))
                        .path(AppUtils.parseString(doc.get(DbKeyConfig.PATH)))
                        .build();
                rows.add(action);
            }
        }
        GetArrayResponse<ApiEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_API, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createApi(CreateApiRequest request) {
        BaseResponse response = new BaseResponse();
        try {

            String path = request.getPath();
            Bson c = Filters.eq(DbKeyConfig.PATH, AppUtils.removeWhitespaceAndLowerCase(path));
            long count = db.countAll(CollectionNameDefs.COLL_API, c);

            if (count > 0) {
                response.setFailed("Path này đã tồn tại");
                return response;
            }

            Document action = new Document();
            action.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            action.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(request.getName()));
            action.append(DbKeyConfig.METHOD, request.getMethod());
            action.append(DbKeyConfig.PATH, AppUtils.removeWhitespaceAndLowerCase(path));
            action.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getName()));
            action.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            action.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_API, action);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

    @Override
    public BaseResponse updateApi(UpdateApiRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_API, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String path = request.getPath();
            Document obj = db.findOne(CollectionNameDefs.COLL_API, Filters.eq(DbKeyConfig.PATH, AppUtils.removeWhitespaceAndLowerCase(path)));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Path này đã tồn tại");
                    return response;
                }
            }

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(request.getName())),
                    Updates.set(DbKeyConfig.METHOD, request.getMethod()),
                    Updates.set(DbKeyConfig.PATH, AppUtils.removeWhitespaceAndLowerCase(path)),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getName())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );

            db.update(CollectionNameDefs.COLL_API, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteApi(DeleteApiRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();

            long count = db.countAll(CollectionNameDefs.COLL_API_ROLE, Filters.eq(DbKeyConfig.APIS, id));
            if (count > 0) {
                response.setFailed("Không thể xóa api này");
                return response;
            }

            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_API, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_API, cond);
            response.setSuccess();
            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
