package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.StatusCV;
import com.edso.resume.api.domain.entities.StatusCVEntity;
import com.edso.resume.api.domain.request.CreateStatusCVRequest;
import com.edso.resume.api.domain.request.DeleteStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateAllStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateStatusCVRequest;
import com.edso.resume.api.domain.response.GetStatusCVResponse;
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

import java.util.*;
import java.util.regex.Pattern;

@Service
public class StatusCVServiceImpl extends BaseService implements StatusCVService {

    public StatusCVServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<StatusCVEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_STATUS_CV, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<StatusCVEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                StatusCVEntity statusCV = StatusCVEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
//                        .children((List<ChildrenStatusCVEntity>) doc.get(DbKeyConfig.CHILDREN))
                        .isDragDisabled((Boolean) doc.get(DbKeyConfig.DELETE))
                        .build();
                rows.add(statusCV);
            }
        }
        GetArrayResponse<StatusCVEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_STATUS_CV, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public GetStatusCVResponse<String> findAllStatusCVProfile(HeaderInfo info) {
        Bson cond = Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations());
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, 0, 0);
        Set<String> rows = new HashSet<>();
        if (lst != null) {
            for (Document doc : lst) {
                if (!Strings.isNullOrEmpty(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)))) {
                    rows.add(AppUtils.parseString(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME))));
                }
            }
        }
        GetStatusCVResponse<String> resp = new GetStatusCVResponse<>();
        resp.setSuccess();
        resp.setTotal(rows.size());
        resp.setRows(rows);
        return resp;
    }

    //RES-293
    @Override
    public GetArrayResponse<StatusCVEntity> findAllStatusCVForRecruitment(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_STATUS_CV, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<StatusCVEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                StatusCVEntity statusCV = StatusCVEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
//                        .children((List<ChildrenStatusCVEntity>) doc.get(DbKeyConfig.CHILDREN))
                        .isDragDisabled((Boolean) doc.get(DbKeyConfig.DELETE))
                        .build();
                rows.add(statusCV);
            }
        }
        GetArrayResponse<StatusCVEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_STATUS_CV, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createStatusCV(CreateStatusCVRequest request, List<String> children) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName();
            Bson c = Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            long count = db.countAll(CollectionNameDefs.COLL_STATUS_CV, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            List<Document> list = new ArrayList<>();
            if (children != null && children.isEmpty()) {
                for (String child : children) {
                    Document idDoc = db.findOne(CollectionNameDefs.COLL_STATUS_CV, Filters.eq(DbKeyConfig.ID, child));
                    if (idDoc == null) {
                        response.setFailed("Id này không tồn tại");
                        return response;
                    } else {
                        Document childrenDoc = new Document();
                        childrenDoc.append(DbKeyConfig.ID, AppUtils.parseString(idDoc.get(DbKeyConfig.ID)));
                        childrenDoc.append(DbKeyConfig.NAME, AppUtils.parseString(idDoc.get(DbKeyConfig.NAME)));
                        list.add(childrenDoc);
                    }
                }
            }

            Document statusCV = new Document();
            statusCV.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            statusCV.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            statusCV.append(DbKeyConfig.CHILDREN, list);
            statusCV.append(DbKeyConfig.DELETE, false);
            statusCV.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            statusCV.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            statusCV.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            statusCV.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            statusCV.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getMyOrganizations());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_STATUS_CV, statusCV);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateStatusCV(UpdateStatusCVRequest request, List<String> children) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_STATUS_CV, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_STATUS_CV, Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            List<Document> list = new ArrayList<>();

            if (children != null && children.isEmpty()) {
                for (String child : children) {
                    Document idDoc = db.findOne(CollectionNameDefs.COLL_STATUS_CV, Filters.eq(DbKeyConfig.ID, child));
                    if (id == null) {
                        response.setFailed("Id này không tồn tại");
                        return response;
                    } else {
                        Document childrenDoc = new Document();
                        childrenDoc.append(DbKeyConfig.ID, AppUtils.parseString(idDoc.get(DbKeyConfig.ID)));
                        childrenDoc.append(DbKeyConfig.NAME, AppUtils.parseString(idDoc.get(DbKeyConfig.NAME)));
                        list.add(childrenDoc);
                    }
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.CHILDREN, list),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_STATUS_CV, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    //RES-295
    @Override
    public BaseResponse updateAllStatusCV(UpdateAllStatusCVRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            db.delete(CollectionNameDefs.COLL_STATUS_CV, null);
            List<StatusCV> list = request.getStatusCVS();
            List<Document> documentList = new ArrayList<>();
            for (StatusCV statusCV : list) {
                Document status = new Document();
                status.append(DbKeyConfig.ID, statusCV.getId());
                status.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(statusCV.getName()));
//                status.append(DbKeyConfig.CHILDREN, list);
                status.append(DbKeyConfig.DELETE, statusCV.getIsDragDisabled());
                status.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(statusCV.getName()));
                status.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(statusCV.getName().toLowerCase()));
                status.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
                status.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
                documentList.add(status);
            }
            db.insertMany(CollectionNameDefs.COLL_STATUS_CV, documentList);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;
        }
    }

    @Override
    public BaseResponse deleteStatusCV(DeleteStatusCVRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_STATUS_CV, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            boolean delete = (boolean) idDocument.get(DbKeyConfig.DELETE);

            if (delete) {
                response.setFailed("Không được xóa bản ghi này!");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_STATUS_CV, cond);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

}