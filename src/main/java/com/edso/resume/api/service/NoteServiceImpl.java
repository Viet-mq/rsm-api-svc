package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.request.CreateNoteProfileRequest;
import com.edso.resume.api.domain.request.DeleteNoteProfileRequest;
import com.edso.resume.api.domain.request.UpdateNoteProfileRequest;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

@Service
public class NoteServiceImpl extends BaseService implements NoteService, IDictionaryValidator {

    private final HistoryService historyService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    @Value("${note.domain}")
    private String domain;

    @Value("${note.serverpath}")
    private String serverPath;

    public NoteServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService) {
        super(db);
        this.historyService = historyService;
    }

    @Override
    public GetArrayResponse<NoteProfileEntity> findAllNote(HeaderInfo info, String idProfile, Integer page, Integer size) {

        GetArrayResponse<NoteProfileEntity> resp = new GetArrayResponse<>();

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
        long total = db.countAll(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_NOTE_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<NoteProfileEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                NoteProfileEntity noteProfile = NoteProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .note(AppUtils.parseString(doc.get(DbKeyConfig.NOTE)))
                        .createAt(AppUtils.parseLong(doc.get(DbKeyConfig.CREATE_AT)))
                        .createBy(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_BY)))
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
    public BaseResponse createNoteProfile(CreateNoteProfileRequest request, MultipartFile file) {

        BaseResponse response = new BaseResponse();

        String key = UUID.randomUUID().toString();
        try {

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, request.getIdProfile(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getUsername(), db, this));
            int total = rs.size();

            for (DictionaryValidateProcessor p : rs) {
                Thread t = new Thread(p);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult result = queue.poll();
                if (result != null) {
                    if (result.getKey().equals(key)) {
                        if (!result.isResult()) {
                            response.setFailed(result.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(result);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            String fullName = null;
            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.USER)) {
                    fullName = r.getResult().getName();
                }
            }

            String url = null;
            String path = null;
            if (file != null) {
                try {
                    saveFile(file);
                    url = domain + file.getOriginalFilename();
                    path = serverPath + file.getOriginalFilename();
                } catch (Throwable ex) {
                    logger.info("Exception: ", ex);
                }
            }

            Document note = new Document();
            note.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            note.append(DbKeyConfig.ID_PROFILE, request.getIdProfile());
            note.append(DbKeyConfig.USERNAME, request.getUsername());
            note.append(DbKeyConfig.FULL_NAME, fullName);
            note.append(DbKeyConfig.EVALUATION, request.getEvaluation());
            note.append(DbKeyConfig.COMMENT, request.getComment());
            note.append(DbKeyConfig.URL, url);
            note.append(DbKeyConfig.PATH, path);
            note.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            note.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_NOTE_PROFILE, note);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(request.getIdProfile(), TypeConfig.CREATE, "Tạo chú ý", request.getInfo().getUsername());

            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
    }

    @Override
    public BaseResponse updateNoteProfile(UpdateNoteProfileRequest request, MultipartFile file) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();
        Bson cond = Filters.eq(DbKeyConfig.ID, request.getId());
        try {

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, request.getIdProfile(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.NOTE, request.getId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getUsername(), db, this));
            int total = rs.size();

            for (DictionaryValidateProcessor p : rs) {
                Thread t = new Thread(p);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult result = queue.poll();
                if (result != null) {
                    if (result.getKey().equals(key)) {
                        if (!result.isResult()) {
                            response.setFailed(result.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(result);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            String fullName = null;
            String path = null;
            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.USER)) {
                    fullName = r.getResult().getName();
                }
                if (r.getResult().getType().equals(ThreadConfig.NOTE)) {
                    path = r.getResult().getName();
                }
            }
            String url = null;
            String newPath = null;
            if(path != null){
                if(file != null){
                    if(!path.equals(serverPath+file.getOriginalFilename())){
                        try {
                            deleteFile(path);
                            saveFile(file);
                            newPath = serverPath + file.getOriginalFilename();
                            url = domain + file.getOriginalFilename();
                        } catch (Throwable ex) {
                            logger.info("Exception: ", ex);
                        }
                    }else url = file.getOriginalFilename();
                }else {
                    deleteFile(path);
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.ID_PROFILE, request.getIdProfile()),
                    Updates.set(DbKeyConfig.USERNAME, request.getUsername()),
                    Updates.set(DbKeyConfig.FULL_NAME, fullName),
                    Updates.set(DbKeyConfig.COMMENT, request.getComment()),
                    Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                    Updates.set(DbKeyConfig.URL, url),
                    Updates.set(DbKeyConfig.PATH, newPath),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_NOTE_PROFILE, cond, updates, true);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(request.getIdProfile(), TypeConfig.UPDATE, "Sửa chú ý", request.getInfo().getUsername());

            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
    }

    @Override
    public BaseResponse deleteNoteProfile(DeleteNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

        //Insert history to DB
        historyService.createHistory(request.getIdProfile(), TypeConfig.DELETE, "Xóa chú ý", request.getInfo().getUsername());

        return new BaseResponse(0, "OK");
    }

    public void saveFile(MultipartFile file) throws IOException {
        File convFile = new File(serverPath + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
    }

    public void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}
