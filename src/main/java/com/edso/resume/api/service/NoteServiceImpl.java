package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.request.CreateNoteProfileRequest;
import com.edso.resume.api.domain.request.DeleteNoteProfileRequest;
import com.edso.resume.api.domain.request.UpdateNoteProfileRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class NoteServiceImpl extends BaseService implements NoteService {

    private final MongoDbOnlineSyncActions db;
    private final HistoryService historyService;

    public NoteServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate) {
        super(db, rabbitTemplate);
        this.db = db;
        this.historyService = historyService;
    }

    @Override
    public GetArrayResponse<NoteProfileEntity> findAllNote(HeaderInfo info, String idProfile, Integer page, Integer size) {

        GetArrayResponse<NoteProfileEntity> resp = new GetArrayResponse<>();

        Bson con = Filters.eq("id", idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if(idProfileDocument == null){
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_NOTE_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<NoteProfileEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                NoteProfileEntity noteProfile = NoteProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .note(AppUtils.parseString(doc.get("note")))
                        .create_at(AppUtils.parseLong(doc.get("create_at")))
                        .create_by(AppUtils.parseString(doc.get("create_by")))
                        .build();
                rows.add(noteProfile);
            }
        }

        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createNoteProfile(CreateNoteProfileRequest request) {

        BaseResponse response = new BaseResponse();

        String idProfile = request.getIdProfile();
        Bson cond = Filters.eq("id", idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
        if(idProfileDocument == null){
            response.setFailed("Id profile không tồn tại");
            return response;
        }

        Document note = new Document();
        note.append("id", UUID.randomUUID().toString());
        note.append("idProfile", idProfile);
        note.append("note", request.getNote());
        note.append("create_at", System.currentTimeMillis());
        note.append("create_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_NOTE_PROFILE, note);
        response.setSuccess();

        //Insert history to DB
        historyService.createHistory(idProfile, "Tạo chú ý", request.getInfo().getFullName());

        return response;
    }

    @Override
    public BaseResponse updateNoteProfile(UpdateNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        String idProfile = request.getIdProfile();
        Bson con = Filters.eq("id", idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if(idProfileDocument == null){
            response.setFailed("Id profile không tồn tại");
            return  response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("idProfile", idProfile),
                Updates.set("note", request.getNote()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_NOTE_PROFILE, cond, updates, true);
        response.setSuccess();

        //Insert history to DB
        historyService.createHistory(idProfile, "Sửa chú ý", request.getInfo().getFullName());

        return response;
    }

    @Override
    public BaseResponse deleteNoteProfile(DeleteNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

        //Insert history to DB
        historyService.createHistory(request.getIdProfile(), "Xóa chú ý", request.getInfo().getFullName());

        return new BaseResponse(0, "OK");
    }
}
