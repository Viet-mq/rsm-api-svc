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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class
DepartmentServiceImpl extends BaseService implements DepartmentService {

    public DepartmentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<DepartmentEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        GetArrayResponse<DepartmentEntity> resp = new GetArrayResponse<>();
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, null, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<DepartmentEntity> rows = new ArrayList<>();
        if (lst != null && lst.iterator().hasNext()) {
            for (Document doc : lst) {

                String idParent = AppUtils.parseString(doc.get(DbKeyConfig.PARENT_ID));
                if (Strings.isNullOrEmpty(idParent)) {
                    DepartmentEntity department = DepartmentEntity.builder()
                            .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
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
        Collections.reverse(rows);
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
    public BaseResponse createDepartment(CreateDepartmentRequest request) {

        BaseResponse response = new BaseResponse();
        try {
//        Document company = db.findOne(CollectionNameDefs.COLL_COMPANY, Filters.eq(DbKeyConfig.ID, request.getIdCompany()));
//        if (company == null) {
//            response.setFailed("Không tồn tại id company này");
//            return response;
//        }
            if (!Strings.isNullOrEmpty(request.getIdParent())) {
                Document doc = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.eq(DbKeyConfig.ID, request.getIdParent()));
                if (doc == null) {
                    response.setFailed("Không tồn tại id parent này");
                    return response;
                }
            }

            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document department = new Document();
            department.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            department.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            department.append(DbKeyConfig.PARENT_ID, request.getIdParent());
            department.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            department.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
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
    public BaseResponse updateDepartment(UpdateDepartmentRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson departmentId = Filters.eq(DbKeyConfig.DEPARTMENT_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, AppUtils.mergeWhitespace(name))
            );
            db.update(CollectionNameDefs.COLL_PROFILE, departmentId, updateProfile);

            Bson updateRecruitment = Updates.combine(
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, AppUtils.mergeWhitespace(name))
            );
            db.update(CollectionNameDefs.COLL_RECRUITMENT, departmentId, updateRecruitment);

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );

            db.update(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond, updates);
            response.setSuccess();
            return response;
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }

    @Override
    public BaseResponse deleteDepartment(DeleteDepartmentRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document department = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.DEPARTMENT_ID, request.getId()));
            if (department != null) {
                response.setFailed("Không thể xóa phòng ban này!");
                return response;
            }
            Document recruitment = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.DEPARTMENT_ID, request.getId()));
            if (recruitment != null) {
                response.setFailed("Không thể xóa phòng ban này!");
                return response;
            }
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond);

            Bson con = Filters.eq(DbKeyConfig.PARENT_ID, id);
            List<Document> parents = db.findAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, con, null, 0, 0);
            if (parents != null) {
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
        db.delete(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, con);
        List<Document> parents = db.findAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, Filters.in(DbKeyConfig.PARENT_ID, parentIds), null, 0, 0);
        if (!lst.isEmpty()) {
            deleteRecursiveFunction(parents, Filters.in(DbKeyConfig.ID, parentIds));
        }
    }

}
