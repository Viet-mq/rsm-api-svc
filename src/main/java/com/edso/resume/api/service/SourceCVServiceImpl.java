package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.SourceCVEntity;
import com.edso.resume.api.domain.request.CreateSourceCVRequest;
import com.edso.resume.api.domain.request.DeleteSourceCVRequest;
import com.edso.resume.api.domain.request.UpdateSourceCVRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.NameConfig;
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
public class SourceCVServiceImpl extends BaseService implements SourceCVService {

    public SourceCVServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<SourceCVEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_SOURCE_CV, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<SourceCVEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                SourceCVEntity sourceCV = SourceCVEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                        .status(AppUtils.parseString(doc.get(DbKeyConfig.STATUS)))
                        .build();
                rows.add(sourceCV);
            }
        }
        GetArrayResponse<SourceCVEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_SOURCE_CV, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createSourceCV(CreateSourceCVRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName().trim();
            Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            long count = db.countAll(CollectionNameDefs.COLL_SOURCE_CV, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document sourceCV = new Document();
            sourceCV.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            sourceCV.append(DbKeyConfig.NAME, name);
            sourceCV.append(DbKeyConfig.EMAIL, request.getEmail());
            sourceCV.append(DbKeyConfig.STATUS, NameConfig.DANG_SU_DUNG);
            sourceCV.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            sourceCV.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            sourceCV.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            sourceCV.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            sourceCV.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_SOURCE_CV, sourceCV);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateSourceCV(UpdateSourceCVRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idSourceCV = Filters.eq(DbKeyConfig.SOURCE_CV_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, request.getName())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, idSourceCV, updateProfile, true);


            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, name),
                    Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                    Updates.set(DbKeyConfig.STATUS, request.getStatus()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_SOURCE_CV, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteSourceCV(DeleteSourceCVRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document source = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.SOURCE_CV_ID, request.getId()));
            if (source == null) {
                String id = request.getId();
                Bson cond = Filters.eq(DbKeyConfig.ID, id);
                Document idDocument = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, cond);

                if (idDocument == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }

                db.delete(CollectionNameDefs.COLL_SOURCE_CV, cond);
                response.setSuccess();
                return response;
            } else {
                response.setFailed("Không thể xóa nguồn cv này!");
                return response;
            }
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }

}