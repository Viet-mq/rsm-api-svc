package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.TagEntity;
import com.edso.resume.api.domain.request.CreateTagRequest;
import com.edso.resume.api.domain.request.DeleteTagRequest;
import com.edso.resume.api.domain.request.UpdateTagRequest;
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

import static com.mongodb.client.model.Updates.set;

@Service
public class TagServiceImpl extends BaseService implements TagService {
    protected TagServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<TagEntity> findAllTag(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> list = db.findAll2(CollectionNameDefs.COLL_TAG, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<TagEntity> rows = new ArrayList<>();
        if (list != null) {
            for (Document document : list) {
                TagEntity tagEntity = TagEntity.builder()
                        .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                        .color(AppUtils.parseString(document.get(DbKeyConfig.COLOR)))
                        .build();
                rows.add(tagEntity);
            }
        }
        GetArrayResponse<TagEntity> response = new GetArrayResponse<>();
        response.setRows(rows);
        response.setTotal(db.countAll(CollectionNameDefs.COLL_TAG, cond));
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse createTag(CreateTagRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document name = db.findOne(CollectionNameDefs.COLL_TAG, Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(request.getName().toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations())));
            if (name != null) {
                response.setFailed("Tên thẻ này đã tồn tại!");
                return response;
            }

            Document tag = new Document();
            tag.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            tag.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(request.getName()));
            tag.append(DbKeyConfig.COLOR, request.getColor());
            tag.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getName()));
            tag.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(request.getName().toLowerCase()));
            tag.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            tag.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            db.insertOne(CollectionNameDefs.COLL_TAG, tag);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }

    @Override
    public BaseResponse updateTag(UpdateTagRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document tag = db.findOne(CollectionNameDefs.COLL_TAG, Filters.eq(DbKeyConfig.ID, request.getId()));
            if (tag == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            Document name = db.findOne(CollectionNameDefs.COLL_TAG, Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(request.getName().toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations())));
            if (name != null && !request.getId().equals(AppUtils.parseString(name.get(DbKeyConfig.ID)))) {
                response.setFailed("Tên thẻ này đã tồn tại");
                return response;
            }

            Bson updates = Updates.combine(
                    set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(request.getName())),
                    set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getName())),
                    set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(request.getName().toLowerCase())),
                    set(DbKeyConfig.COLOR, request.getColor()),
                    set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );

            db.update(CollectionNameDefs.COLL_TAG, Filters.eq(DbKeyConfig.ID, request.getId()), updates, true);

            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }

    @Override
    public BaseResponse deleteTag(DeleteTagRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.TAGS, request.getId()));
            if (profile == null) {
                Document tag = db.findOne(CollectionNameDefs.COLL_TAG, Filters.eq(DbKeyConfig.ID, request.getId()));
                if (tag == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
                db.delete(CollectionNameDefs.COLL_TAG, Filters.eq(DbKeyConfig.ID, request.getId()));
                response.setSuccess();
                return response;
            }
            response.setFailed("Không thể xóa thẻ này");
            return response;
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }
}
