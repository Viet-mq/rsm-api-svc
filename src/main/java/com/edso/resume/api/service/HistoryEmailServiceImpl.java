package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.FileEntity;
import com.edso.resume.api.domain.entities.HistoryEmailEntity;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryEmailServiceImpl extends BaseService implements HistoryEmailService {
    protected HistoryEmailServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Value("${mail.serverPath}")
    private String serverPath;

    @Override
    public List<String> createHistoryEmail(String historyId, String profileId, String subject, String content, List<MultipartFile> files, HeaderInfo info) {
        List<String> listPath = new ArrayList<>();
        List<Document> listDocument = new ArrayList<>();
        try {
            Document fullName = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String path = AppUtils.saveFile(serverPath, file);
                        listPath.add(path);
                        Document pathDocument = new Document();
                        pathDocument.append(DbKeyConfig.FILE_NAME, file.getOriginalFilename());
                        pathDocument.append(DbKeyConfig.PATH_FILE, path);
                        listDocument.add(pathDocument);
                    }
                }
            }

            Document history = new Document();
            history.append(DbKeyConfig.ID, history);
            history.append(DbKeyConfig.ID_PROFILE, profileId);
            history.append(DbKeyConfig.SUBJECT, subject);
            history.append(DbKeyConfig.CONTENT, content);
            history.append(DbKeyConfig.TIME, System.currentTimeMillis());
            history.append(DbKeyConfig.USERNAME, info.getUsername());
            history.append(DbKeyConfig.STATUS, "Đang đợi gửi");
//            history.append(DbKeyConfig.FULL_NAME, fullName.get(DbKeyConfig.FULL_NAME));
            history.append(DbKeyConfig.FILE, listDocument);

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_HISTORY_EMAIL, history);
            logger.info("createHistoryEmail history: {}", history);
            return listPath;
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            return null;
        }
    }

    @Override
    public List<String> createHistoryEmails(List<IdEntity> ids, String subject, String content, List<MultipartFile> files, HeaderInfo info) {
        List<String> listPath = new ArrayList<>();
        List<Document> listDocument = new ArrayList<>();
        try {
            Document fullName = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String path = AppUtils.saveFile(serverPath, file);
                        listPath.add(path);
                        Document pathDocument = new Document();
                        pathDocument.append(DbKeyConfig.FILE_NAME, file.getOriginalFilename());
                        pathDocument.append(DbKeyConfig.PATH_FILE, path);
                        listDocument.add(pathDocument);
                    }
                }
            }

            List<Document> historyList = new ArrayList<>();
            for (IdEntity id : ids) {
                String historyId = UUID.randomUUID().toString();
                id.setHistoryId(historyId);
                Document history = new Document();
                history.append(DbKeyConfig.ID, history);
                history.append(DbKeyConfig.ID_PROFILE, id.getProfileId());
                history.append(DbKeyConfig.SUBJECT, subject);
                history.append(DbKeyConfig.CONTENT, content);
                history.append(DbKeyConfig.TIME, System.currentTimeMillis());
                history.append(DbKeyConfig.USERNAME, info.getUsername());
                history.append(DbKeyConfig.STATUS, "Đang đợi gửi");
//                history.append(DbKeyConfig.FULL_NAME, fullName.get(DbKeyConfig.FULL_NAME));
                history.append(DbKeyConfig.FILE, listDocument);
                historyList.add(history);
            }

            // insert to database
            db.insertMany(CollectionNameDefs.COLL_HISTORY_EMAIL, historyList);
            logger.info("createHistoryEmails historyList: {}", historyList);
            return listPath;
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            return null;
        }
    }

    @Override
    public void deleteHistoryEmail(String idProfile) {
        db.delete(CollectionNameDefs.COLL_HISTORY_EMAIL, Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        logger.info("deleteHistoryEmail idProfile: {}", idProfile);
    }

    @Override
    public GetArrayResponse<HistoryEmailEntity> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size) {
        GetArrayResponse<HistoryEmailEntity> resp = new GetArrayResponse<>();

        Bson con = Filters.eq(DbKeyConfig.ID, idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if (idProfileDocument == null) {
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }

        Bson cond = Filters.eq(DbKeyConfig.ID_PROFILE, idProfile);
        Bson sort = Filters.eq(DbKeyConfig.TIME, -1);

        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_HISTORY_PROFILE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<HistoryEmailEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                List<FileEntity> list = new ArrayList<>();
                List<Document> documentList = (List<Document>) doc.get(DbKeyConfig.FILE);
                if (documentList != null && !documentList.isEmpty()) {
                    for (Document document : documentList) {
                        FileEntity file = FileEntity.builder()
                                .fileName(AppUtils.parseString(document.get(DbKeyConfig.FILE_NAME)))
                                .filePath(AppUtils.parseString(document.get(DbKeyConfig.PATH_FILE)))
                                .build();
                        list.add(file);
                    }
                }

                HistoryEmailEntity history = HistoryEmailEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .subject(AppUtils.parseString(doc.get(DbKeyConfig.SUBJECT)))
                        .content(AppUtils.parseString(doc.get(DbKeyConfig.CONTENT)))
                        .status(AppUtils.parseString(doc.get(DbKeyConfig.STATUS)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .files(list)
                        .build();
                rows.add(history);
            }
        }

        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_HISTORY_EMAIL, cond));
        resp.setRows(rows);
        return resp;
    }
}
