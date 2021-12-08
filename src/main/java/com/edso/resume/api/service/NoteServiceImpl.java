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
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

@Service
public class NoteServiceImpl extends BaseService implements NoteService, IDictionaryValidator {

    private final HistoryService historyService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    @Value("${note.serverpath}")
    private String serverPath;
    @Value("${note.domain}")
    private String domain;
    @Value("${note.fileSize}")
    private Long fileSize;

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
        MultipartFile file = request.getFile();
        if (file != null && file.getSize() > fileSize) {
            return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
        }
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
                    fullName = (String) r.getResult().getName();
                }
            }

            String fileName = null;
            String pathFile = null;
            String url = null;
            if (file != null) {
                try {
                    fileName = saveFile(file);
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

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_NOTE_PROFILE, note);
            response.setSuccess();

            //Insert history to DB
            historyService.createHistory(request.getIdProfile(), TypeConfig.CREATE, "Tạo chú ý",request.getInfo());

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
        MultipartFile file = request.getFile();
        if (file != null && file.getSize() > fileSize) {
            return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
        }
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();
        try {
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
            String idProfile = null;
            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.USER)) {
                    fullName = (String) r.getResult().getName();
                }
                if (r.getResult().getType().equals(ThreadConfig.NOTE)) {
                    pathFile = (String) r.getResult().getName();
                    idProfile = r.getResult().getIdProfile()
                    ;
                }
            }

            String fileName = null;
            String url = null;
            String pathFile1 = null;
            if (file != null) {
                try {
                    deleteFile(pathFile);
                    fileName = saveFile(file);
                    url = domain + fileName;
                    pathFile1 = serverPath + fileName;
                } catch (Throwable ex) {
                    logger.error("Exception: ", ex);
                }
            } else {
                if (pathFile != null) {
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

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Sửa chú ý",request.getInfo());

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
            deleteFile(path);

            db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);

            //Insert history to DB
            historyService.createHistory(AppUtils.parseString(idDocument.get(DbKeyConfig.ID_PROFILE)), TypeConfig.DELETE, "Xóa chú ý",request.getInfo());
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
            deleteFile(path);
        }
        //Xóa note
        db.delete(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
    }

    public String saveFile(MultipartFile file) {
        FileOutputStream fos = null;
        try {
            String fileName = file.getOriginalFilename();
            File file1 = new File(serverPath + fileName);
            int i = 0;
            while (file1.exists()) {
                i++;
                String[] arr = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
                fileName = arr[0] + " (" + i + ")." + arr[1];
                file1 = new File(serverPath + fileName);
            }
            fos = new FileOutputStream(file1);
            fos.write(file.getBytes());
            return fileName;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable ex) {
                    logger.error("Exception: ", ex);
                }
            }
        }
        return null;
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
