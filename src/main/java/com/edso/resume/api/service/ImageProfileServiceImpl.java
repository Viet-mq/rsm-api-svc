package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.request.DeleteImageProfileRequest;
import com.edso.resume.api.domain.request.UpdateImageProfileRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

@Service
public class ImageProfileServiceImpl extends BaseService implements ImageProfileService {

    @Value("${avatar.serverpath}")
    private String serverPath;
    @Value("${avatar.domain}")
    private String domain;
    @Value("${avatar.fileSize}")
    private Long fileSize;

    protected ImageProfileServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public BaseResponse updateImageProfile(UpdateImageProfileRequest request) {
        MultipartFile image = request.getImage();
        BaseResponse response = new BaseResponse();
        if (image.getSize() > fileSize) {
            response.setFailed("Dung lượng file quá lớn");
            return response;
        }
        Bson cond = Filters.eq(DbKeyConfig.ID, request.getIdProfile());
        Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (profile == null) {
            response.setFailed("Không tồn tại id profile này");
            return response;
        }

        String fileName = saveFile(image);

        Bson update = Updates.combine(
                Updates.set(DbKeyConfig.URL_IMAGE, domain + fileName),
                Updates.set(DbKeyConfig.PATH_IMAGE, serverPath + fileName)
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, update, true);

        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteImageProfile(DeleteImageProfileRequest request) {
        BaseResponse response = new BaseResponse();
        Bson cond = Filters.eq(DbKeyConfig.ID, request.getIdProfile());
        Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (profile == null) {
            response.setFailed("Không tồn tại id profile này");
            return response;
        }

        deleteFile(AppUtils.parseString(profile.get(DbKeyConfig.PATH_IMAGE)));

        Bson update = Updates.combine(
                Updates.set(DbKeyConfig.URL_IMAGE, null),
                Updates.set(DbKeyConfig.PATH_IMAGE, null)
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, update, true);

        response.setSuccess();
        return null;
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
}
