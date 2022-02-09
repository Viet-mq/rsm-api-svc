package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CommentEntity;
import com.edso.resume.api.domain.request.CreateCommentRequest;
import com.edso.resume.api.domain.request.DeleteCommentRequest;
import com.edso.resume.api.domain.request.UpdateCommentRequest;
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
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CommentServiceImpl extends BaseService implements CommentService {
    protected CommentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<CommentEntity> findAllComment(HeaderInfo info, String idProfile, Integer page, Integer size) {
        GetArrayResponse<CommentEntity> resp = new GetArrayResponse<>();

        Bson con = Filters.eq(DbKeyConfig.ID, idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if (idProfileDocument == null) {
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex(DbKeyConfig.ID_PROFILE, Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        Bson sort = Filters.eq(DbKeyConfig.FULL_NAME, 1);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_COMMENT, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<CommentEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CommentEntity noteProfile = CommentEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .content(AppUtils.parseString(doc.get(DbKeyConfig.CONTENT)))
                        .createAt(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_AT)))
                        .createBy(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_BY)))
                        .updateAt(AppUtils.parseString(doc.get(DbKeyConfig.UPDATE_AT)))
                        .updateBy(AppUtils.parseString(doc.get(DbKeyConfig.UPDATE_BY)))
                        .build();
                rows.add(noteProfile);
            }
        }

        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_COMMENT, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createCommentProfile(CreateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, request.getIdProfile()));

            if (profile == null) {
                response.setFailed("Id profile này không tồn tại!");
                return response;
            }

            Document job = new Document();
            job.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            job.append(DbKeyConfig.CONTENT, AppUtils.mergeWhitespace(request.getContent()));
            job.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            job.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_COMMENT, job);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;
        }
    }

    @Override
    public BaseResponse updateCommentProfile(UpdateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, request.getIdProfile()));

            if (profile == null) {
                response.setFailed("Id profile này không tồn tại!");
                return response;
            }

            Document comment = db.findOne(CollectionNameDefs.COLL_COMMENT, Filters.eq(DbKeyConfig.ID, request.getId()));

            if (comment == null) {
                response.setFailed("Id comment này không tồn tại!");
                return response;
            }

            Document job = new Document();
            job.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            job.append(DbKeyConfig.CONTENT, AppUtils.mergeWhitespace(request.getContent()));
            job.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            job.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_COMMENT, job);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }

    }

    @Override
    public BaseResponse deleteCommentProfile(DeleteCommentRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_COMMENT, cond);

            if (idDocument == null) {
                response.setFailed("Id comment này không tồn tại");
                return response;
            }

            db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }

    }


    @Override
    public void deleteCommentProfileByIdProfile(String idProfile) {
        Bson cond = Filters.eq(DbKeyConfig.ID_PROFILE, idProfile);
        db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
    }
}
