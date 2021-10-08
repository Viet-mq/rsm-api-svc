package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.request.CreateCommentRequest;
import com.edso.resume.api.domain.request.UpdateCommentRequest;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentServiceImpl extends BaseService implements CommentService {

    private final MongoDbOnlineSyncActions db;

    public CommentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
        this.db = db;
    }


    @Override
    public BaseResponse createComment(CreateCommentRequest request) {
        BaseResponse response = new BaseResponse();

        Document comment = new Document();

        comment.append("id", UUID.randomUUID().toString());
        comment.append("idProfile", request.getIdProfile());
        comment.append("name", request.getName());
        comment.append("comment", request.getComment());
        comment.append("create_at", System.currentTimeMillis());
        comment.append("update_at", System.currentTimeMillis());
        comment.append("create_by", request.getInfo().getUsername());
        comment.append("update_by", request.getInfo().getUsername());

        db.insertOne(CollectionNameDefs.COLL_COMMENT, comment);

        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse updateComment(UpdateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_COMMENT, cond);
        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        Bson updates = Updates.combine(
                Updates.set("idProfile", request.getIdProfile()),
                Updates.set("name", request.getName()),
                Updates.set("comment", request.getComment())
        );

        db.update(CollectionNameDefs.COLL_COMMENT, cond, updates);
        response.setSuccess();

        return response;
    }
}
