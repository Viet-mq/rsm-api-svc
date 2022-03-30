package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.OrganizationEntity;
import com.edso.resume.api.domain.request.CreateOrganizationRequest;
import com.edso.resume.api.domain.request.DeleteOrganizationRequest;
import com.edso.resume.api.domain.request.UpdateOrganizationRequest;
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
public class OrganizationServiceImpl extends BaseService implements OrganizationService {
    protected OrganizationServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<OrganizationEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_ORGANIZATION, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<OrganizationEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                OrganizationEntity organization = OrganizationEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .description(AppUtils.parseString(doc.get(DbKeyConfig.DESCRIPTION)))
                        .organizations((List<String>) doc.get(DbKeyConfig.ORGANIZATIONS))
                        .build();
                rows.add(organization);
            }
        }
        GetArrayResponse<OrganizationEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_ORGANIZATION, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createOrganization(CreateOrganizationRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_ORGANIZATION, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            List<Document> departments = db.findAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.in(DbKeyConfig.ID, request.getOrganizations()), null, 0, 0);
            if (departments.size() != request.getOrganizations().size()) {
                response.setFailed("Không tồn tại tổ chức này");
                return response;
            }

            Document organization = new Document();
            organization.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            organization.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            organization.append(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription()));
            organization.append(DbKeyConfig.ORGANIZATIONS, request.getOrganizations());
            organization.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            organization.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            organization.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            organization.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            organization.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            organization.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            organization.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_ORGANIZATION, organization);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateOrganization(UpdateOrganizationRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ORGANIZATION, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_ORGANIZATION, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            List<Document> departments = db.findAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.in(DbKeyConfig.ID, request.getOrganizations()), null, 0, 0);
            if (departments.size() != request.getOrganizations().size()) {
                response.setFailed("Không tồn tại tổ chức này");
                return response;
            }

            Bson idOrganization = Filters.eq(DbKeyConfig.ORGANIZATION_ID, request.getId());
            Bson updateUser = Updates.combine(
                    Updates.set(DbKeyConfig.ORGANIZATION_NAME, AppUtils.mergeWhitespace(name))
            );
            db.update(CollectionNameDefs.COLL_USER, idOrganization, updateUser);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.DESCRIPTION, AppUtils.mergeWhitespace(request.getDescription())),
                    Updates.set(DbKeyConfig.ORGANIZATIONS, request.getOrganizations()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_ORGANIZATION, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteOrganization(DeleteOrganizationRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Document document = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.ORGANIZATION_ID, id));
            if (document != null) {
                response.setFailed("Không thể xóa tổ chức này");
                return response;
            }
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ORGANIZATION, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_ORGANIZATION, cond);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
