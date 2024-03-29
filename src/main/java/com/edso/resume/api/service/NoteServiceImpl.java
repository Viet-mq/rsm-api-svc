package com.edso.resume.api.service;

import com.edso.resume.api.common.DocToPdfConverter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class NoteServiceImpl extends BaseService implements NoteService, IDictionaryValidator {

    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    @Value("${note.serverPath}")
    private String serverPath;
    @Value("${note.domain}")
    private String domain;
    @Value("${note.fileSize}")
    private Long fileSize;

    public NoteServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<NoteProfileEntity> findAllNote(HeaderInfo info, String idProfile, Integer page, Integer size) {

        GetArrayResponse<NoteProfileEntity> resp = new GetArrayResponse<>();
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        Bson sort = Filters.eq(DbKeyConfig.FULL_NAME, 1);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_NOTE_PROFILE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<NoteProfileEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                NoteProfileEntity noteProfile = NoteProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .comment(AppUtils.parseString(doc.get(DbKeyConfig.COMMENT)))
                        .evaluation(AppUtils.parseString(doc.get(DbKeyConfig.EVALUATION)))
                        .fileName(AppUtils.parseString(doc.get(DbKeyConfig.FILE_NAME)))
                        .url(AppUtils.parseString(doc.get(DbKeyConfig.URL)))
                        .updateAt(AppUtils.parseString(doc.get(DbKeyConfig.UPDATE_AT)))
                        .updateBy(AppUtils.parseString(doc.get(DbKeyConfig.UPDATE_BY)))
                        .build();
                rows.add(noteProfile);
            }
        }

        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_NOTE_PROFILE, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createNoteProfile(CreateNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();
        try {
            MultipartFile file = request.getFile();
            if (file != null && file.getSize() > fileSize) {
                return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
            }
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
                            response.setFailed((String) result.getName());
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
                    fullName = r.getResult().getFullName();
                }
            }

            String fileName = null;
            String pathFile = null;
            String url = null;
            if (file != null && !file.isEmpty()) {
                try {
                    String type = file.getOriginalFilename().split("\\.")[1];
                    if (type.equals("doc") || type.equals("docx")) {
                        fileName = DocToPdfConverter.convertWordToPdf(serverPath, file);
                    } else {
                        fileName = FileUtils.saveFile(serverPath, file);
                    }
                    pathFile = serverPath + fileName;
                    url = domain + fileName;
                } catch (Throwable ex) {
                    logger.error("Exception: ", ex);
                }
            }

            Document note = new Document();
            note.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            note.append(DbKeyConfig.ID_PROFILE, request.getIdProfile());
            note.append(DbKeyConfig.USERNAME, request.getUsername());
            note.append(DbKeyConfig.FULL_NAME, fullName);
            note.append(DbKeyConfig.EVALUATION, request.getEvaluation());
            note.append(DbKeyConfig.COMMENT, request.getComment());
            note.append(DbKeyConfig.FILE_NAME, fileName);
            note.append(DbKeyConfig.PATH_FILE, pathFile);
            note.append(DbKeyConfig.URL, url);
            note.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            note.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            note.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            note.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());
            note.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getMyOrganizations());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_NOTE_PROFILE, note);
            response.setSuccess();
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
    public BaseResponse updateNoteProfile(UpdateNoteProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();
        try {
            MultipartFile file = request.getFile();
            if (file != null && file.getSize() > fileSize) {
                return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
            }
            Bson cond = Filters.eq(DbKeyConfig.ID, request.getId());
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
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
                            response.setFailed((String) result.getName());
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
            String pathFile = null;
            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.USER)) {
                    fullName = r.getResult().getFullName();
                }
                if (r.getResult().getType().equals(ThreadConfig.NOTE)) {
                    pathFile = (String) r.getResult().getName();
                }
            }

            String fileName = null;
            String url = null;
            String pathFile1 = null;
            if (file != null && !file.isEmpty()) {
                try {
                    if (!Strings.isNullOrEmpty(pathFile)) {
                        deleteFile(pathFile);
                    }
                    String type = file.getOriginalFilename().split("\\.")[1];
                    if (type.equals("doc") || type.equals("docx")) {
                        fileName = DocToPdfConverter.convertWordToPdf(serverPath, file);
                    } else {
                        fileName = FileUtils.saveFile(serverPath, file);
                    }
                    url = domain + fileName;
                    pathFile1 = serverPath + fileName;
                } catch (Throwable ex) {
                    logger.error("Exception: ", ex);
                }
            } else {
                if (!Strings.isNullOrEmpty(pathFile)) {
                    deleteFile(pathFile);
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.USERNAME, request.getUsername()),
                    Updates.set(DbKeyConfig.FULL_NAME, fullName),
                    Updates.set(DbKeyConfig.COMMENT, request.getComment()),
                    Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                    Updates.set(DbKeyConfig.FILE_NAME, fileName),
                    Updates.set(DbKeyConfig.PATH_FILE, pathFile1),
                    Updates.set(DbKeyConfig.URL, url),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_NOTE_PROFILE, cond, updates, true);
            response.setSuccess();
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
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            //Xóa file
            String path = AppUtils.parseString(idDocument.get(DbKeyConfig.PATH_FILE));
            if (!Strings.isNullOrEmpty(path)) {
                deleteFile(path);
            }

            db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        return new BaseResponse(0, "OK");
    }

    @Override
    public void deleteNoteProfileByIdProfile(String idProfile) {
        Bson cond = Filters.eq(DbKeyConfig.ID_PROFILE, idProfile);
        FindIterable<Document> list = db.findAll2(CollectionNameDefs.COLL_NOTE_PROFILE, cond, null, 0, 0);

        //Xóa file
        for (Document doc : list) {
            String path = AppUtils.parseString(doc.get(DbKeyConfig.PATH_FILE));
            if (!Strings.isNullOrEmpty(path)) {
                deleteFile(path);
            }
        }
        //Xóa note
        db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
    }

    public void deleteFile(String path) {
        File file = new File(path);
        if (file.delete()) {
            logger.info("deleteFile filePath:{}", path);
        }
    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}
