package com.edso.resume.api.service;


import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.OrganizationEntity;
import com.edso.resume.api.domain.entities.SubOrganization;
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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class
OrganizationServiceImpl extends BaseService implements OrganizationService {

    public OrganizationServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<OrganizationEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        try {
            GetArrayResponse<OrganizationEntity> resp = new GetArrayResponse<>();
            Bson cond = Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name)));
            PagingInfo pagingInfo = PagingInfo.parse(page, size);
            List<Document> lst = db.findAll(CollectionNameDefs.COLL_ORGANIZATION, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
            List<OrganizationEntity> rows = new ArrayList<>();
            if (!lst.isEmpty()) {
                while (!lst.isEmpty()) {
                    Document doc = lst.get(0);
                    String id = AppUtils.parseString(doc.get(DbKeyConfig.ID));
                    OrganizationEntity organization = OrganizationEntity.builder()
                            .id(id)
                            .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                            .build();
                    lst.remove(doc);
                    List<SubOrganization> listSub = new ArrayList<>();
                    List<Document> removes = new ArrayList<>();
                    for (Document document : lst) {
                        if (id.equals(AppUtils.parseString(document.get(DbKeyConfig.PARENT_ID)))) {
                            SubOrganization subOrganizationEntity = SubOrganization.builder()
                                    .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                                    .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                                    .build();
                            listSub.add(subOrganizationEntity);
                            removes.add(document);
                        }
                    }
                    for (Document document : removes) {
                        lst.remove(document);
                    }
                    if (!listSub.isEmpty()) {
                        organization.setChildren(listSub);
                    }
                    a(listSub, lst);
                    rows.add(organization);
                }
            }
            resp.setSuccess();
            resp.setTotal(db.countAll(CollectionNameDefs.COLL_ORGANIZATION, null));
            Collections.reverse(rows);
            resp.setRows(rows);
            return resp;
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            return null;
        }
    }


    private void a(List<SubOrganization> listSub, List<Document> lst) {
        if (listSub != null) {
            for (SubOrganization sub : listSub) {
                List<SubOrganization> subs = new ArrayList<>();
                List<Document> listRemove = new ArrayList<>();
                for (Document document : lst) {
                    if (sub.getId().equals(AppUtils.parseString(document.get(DbKeyConfig.PARENT_ID)))) {
                        SubOrganization subOrganizationEntity = SubOrganization.builder()
                                .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                                .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                                .build();
                        subs.add(subOrganizationEntity);
                        listRemove.add(document);
                    }
                }
                for (Document document : listRemove) {
                    lst.remove(document);
                }
                if (!subs.isEmpty()) {
                    sub.setChildren(subs);
                }
                a(subs, lst);
            }
        }
    }

    @Override
    public BaseResponse createOrganization(CreateOrganizationRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            if (!Strings.isNullOrEmpty(request.getIdParent())) {
                Document doc = db.findOne(CollectionNameDefs.COLL_ORGANIZATION, Filters.eq(DbKeyConfig.ID, request.getIdParent()));
                if (doc == null) {
                    response.setFailed("Không tồn tại id parent này");
                    return response;
                }
            }

            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_ORGANIZATION, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document organization = new Document();
            organization.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            organization.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            organization.append(DbKeyConfig.PARENT_ID, request.getIdParent());
            organization.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            organization.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            organization.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            organization.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_ORGANIZATION, organization);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
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

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );

            db.update(CollectionNameDefs.COLL_ORGANIZATION, cond, updates);
            response.setSuccess();
            return response;
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }

    @Override
    public BaseResponse deleteOrganization(DeleteOrganizationRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ORGANIZATION, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_ORGANIZATION, cond);

            Bson con = Filters.eq(DbKeyConfig.PARENT_ID, id);
            List<Document> parents = db.findAll(CollectionNameDefs.COLL_ORGANIZATION, con, null, 0, 0);
            if (!parents.isEmpty()) {
                deleteRecursiveFunction(parents, con);
            }

            response.setSuccess();
            return response;
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }

    public void deleteRecursiveFunction(List<Document> lst, Bson con) {
        List<String> parentIds = new ArrayList<>();
        for (Document document : lst) {
            parentIds.add(AppUtils.parseString(document.get(DbKeyConfig.ID)));
        }
        db.delete(CollectionNameDefs.COLL_ORGANIZATION, con);
        List<Document> parents = db.findAll(CollectionNameDefs.COLL_ORGANIZATION, Filters.in(DbKeyConfig.PARENT_ID, parentIds), null, 0, 0);
        if (!lst.isEmpty()) {
            deleteRecursiveFunction(parents, Filters.in(DbKeyConfig.ID, parentIds));
        }
    }
}

