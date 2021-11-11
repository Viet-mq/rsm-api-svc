package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DepartmentEntity;
import com.edso.resume.api.domain.entities.SubDepartmentEntity;
import com.edso.resume.api.domain.request.CreateDepartmentRequest;
import com.edso.resume.api.domain.request.DeleteDepartmentRequest;
import com.edso.resume.api.domain.request.UpdateDepartmentRequest;
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

@Service
public class DepartmentServiceImpl extends BaseService implements DepartmentService {

    public DepartmentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<DepartmentEntity> findAll(HeaderInfo info, String idCompany, String name, Integer page, Integer size) {
        GetArrayResponse<DepartmentEntity> resp = new GetArrayResponse<>();
//        Bson cond = Filters.eq(DbKeyConfig.COMPANY_ID, idCompany);
//        Document company = db.findOne(CollectionNameDefs.COLL_COMPANY, Filters.eq(DbKeyConfig.ID, idCompany));
//        if (company == null) {
//            resp.setFailed("Không tồn tại công ty này");
//            return resp;
//        }
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, null, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<DepartmentEntity> rows = new ArrayList<>();
        if (lst != null && lst.iterator().hasNext()) {
            for (Document doc : lst) {
                String idParent = AppUtils.parseString(doc.get(DbKeyConfig.PARENT_ID));
                if (Strings.isNullOrEmpty(idParent)) {
                    DepartmentEntity department = DepartmentEntity.builder()
                            .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                            .idCompany(AppUtils.parseString(doc.get(DbKeyConfig.COMPANY_ID)))
                            .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                            .build();
                    rows.add(department);
                } else {
                    for (DepartmentEntity departmentEntity : rows) {
                        if (departmentEntity.getId().equals(idParent)) {
                            List<SubDepartmentEntity> list = departmentEntity.getChildren();
                            if (list == null) {
                                list = new ArrayList<>();
                            }
                            SubDepartmentEntity subDepartmentEntity = SubDepartmentEntity.builder()
                                    .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                                    .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                                    .build();

                            //De quy
                            recursiveFunction(lst, subDepartmentEntity);

                            list.add(subDepartmentEntity);
                            departmentEntity.setChildren(list);
                        }
                    }
                }
            }
        }
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, null));
        resp.setRows(rows);
        return resp;
    }

    public void recursiveFunction(FindIterable<Document> lst, SubDepartmentEntity subs) {
        for (Document doc : lst) {
            String idParent = AppUtils.parseString(doc.get(DbKeyConfig.PARENT_ID));
            if (!Strings.isNullOrEmpty(idParent)) {
                if (subs.getId().equals(idParent)) {
                    List<SubDepartmentEntity> list = subs.getChildren();
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    SubDepartmentEntity subDepartmentEntity = SubDepartmentEntity.builder()
                            .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                            .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                            .build();

                    recursiveFunction(lst, subDepartmentEntity);

                    list.add(subDepartmentEntity);
                    subs.setChildren(list);
                }
            }
        }
    }

    @Override
    public BaseResponse createDepartment(CreateDepartmentRequest request, String idParent) {

        BaseResponse response = new BaseResponse();
        try {
//        Document company = db.findOne(CollectionNameDefs.COLL_COMPANY, Filters.eq(DbKeyConfig.ID, request.getIdCompany()));
//        if (company == null) {
//            response.setFailed("Không tồn tại id company này");
//            return response;
//        }
            String parentName = null;
            if (!Strings.isNullOrEmpty(idParent)) {
                Document doc = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.eq(DbKeyConfig.ID, idParent));
                if (doc != null) {
                    parentName = AppUtils.parseString(doc.get(DbKeyConfig.NAME));
                } else {
                    response.setFailed("Không tồn tại id parent này");
                    return response;
                }
            }

            String name = request.getName().trim();
            Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            long count = db.countAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document department = new Document();
            department.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            department.append(DbKeyConfig.NAME, request.getName());
            department.append(DbKeyConfig.COMPANY_ID, null);
            department.append(DbKeyConfig.PARENT_ID, idParent);
            department.append(DbKeyConfig.PARENT_NAME, parentName);
            department.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            department.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            department.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, department);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateDepartment(UpdateDepartmentRequest request, String idParent) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idJobLevel = Filters.eq(DbKeyConfig.DEPARTMENT_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, request.getName())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, idJobLevel, updateProfile, true);

            Bson updates;
            if (!Strings.isNullOrEmpty(idParent)) {
                Document doc = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.eq(DbKeyConfig.PARENT_ID, idParent));
                if (doc != null) {
                    String parentName = AppUtils.parseString(doc.get(DbKeyConfig.NAME));
                    // update roles
                    updates = Updates.combine(
                            Updates.set(DbKeyConfig.NAME, request.getName()),
                            Updates.set(DbKeyConfig.PARENT_NAME, parentName),
                            Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                            Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                            Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
                    );
                } else {
                    response.setFailed("Không tồn tại id parent này");
                    return response;
                }
            } else {
                if (Strings.isNullOrEmpty(AppUtils.parseString(idDocument.get(DbKeyConfig.PARENT_ID)))) {
                    // update roles
                    updates = Updates.combine(
                            Updates.set(DbKeyConfig.NAME, request.getName()),
                            Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                            Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                            Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
                    );

                    Bson con = Filters.eq(DbKeyConfig.PARENT_ID, request.getId());
                    Bson update = Updates.combine(
                            Updates.set(DbKeyConfig.PARENT_NAME, request.getName())
                    );
                    db.update(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, con, update, true);
                } else {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
            }

            db.update(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond, updates, true);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteDepartment(DeleteDepartmentRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
        return new BaseResponse(0, "OK");
    }
}
