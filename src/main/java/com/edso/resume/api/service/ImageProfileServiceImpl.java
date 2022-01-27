package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.EventImageEntity;
import com.edso.resume.api.domain.entities.ImageEntity;
import com.edso.resume.api.domain.request.DeleteImageProfileRequest;
import com.edso.resume.api.domain.request.UpdateImageProfileRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

@Service
public class ImageProfileServiceImpl extends BaseService implements ImageProfileService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${avatar.serverPath}")
    private String serverPath;
    @Value("${avatar.domain}")
    private String domain;
    @Value("${avatar.fileSize}")
    private Long fileSizeAvatar;
    @Value("${avatar.fileSize}")
    private Long fileSize;
    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;

    protected ImageProfileServiceImpl(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate) {
        super(db);
        this.rabbitTemplate = rabbitTemplate;
    }

    private void publishActionToRabbitMQ(String type, ImageEntity image) {
        EventImageEntity eventImageEntity = new EventImageEntity(type, image);
        rabbitTemplate.convertAndSend(exchange, routingkey, eventImageEntity);
        logger.info("=>publishActionToRabbitMQ type: {}, image: {}", type, image);
    }

    @Override
    public BaseResponse updateImageProfile(UpdateImageProfileRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            MultipartFile image = request.getImage();
            if (image.getSize() > fileSizeAvatar) {
                return new BaseResponse(ErrorCodeDefs.IMAGE, "File vượt quá dung lượng cho phép");
            }
            Bson cond = Filters.eq(DbKeyConfig.ID, request.getIdProfile());
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

            if (profile == null) {
                response.setFailed("Không tồn tại id profile này");
                return response;
            }

            String path = AppUtils.parseString(profile.get(DbKeyConfig.PATH_IMAGE));
            if (!Strings.isNullOrEmpty(path)) {
                deleteFile(path);
            }
            String fileName = saveFile(image);

            Bson update = Updates.combine(
                    Updates.set(DbKeyConfig.URL_IMAGE, domain + fileName),
                    Updates.set(DbKeyConfig.PATH_IMAGE, serverPath + fileName)
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, update, true);


            ImageEntity imageEntity = new ImageEntity(request.getIdProfile(), domain + fileName);
            publishActionToRabbitMQ("update-image", imageEntity);

//        rabbit.publishImageToRabbit("update-image", imageEntity);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteImageProfile(DeleteImageProfileRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Bson cond = Filters.eq(DbKeyConfig.ID, request.getIdProfile());
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

            if (profile == null) {
                response.setFailed("Không tồn tại id profile này");
                return response;
            }

            String path = AppUtils.parseString(profile.get(DbKeyConfig.PATH_IMAGE));
            if (!Strings.isNullOrEmpty(path)) {
                deleteFile(path);
            }

            Bson update = Updates.combine(
                    Updates.set(DbKeyConfig.URL_IMAGE, null),
                    Updates.set(DbKeyConfig.PATH_IMAGE, null)
            );

            db.update(CollectionNameDefs.COLL_PROFILE, cond, update, true);

            ImageEntity imageEntity = new ImageEntity(request.getIdProfile(), null);
            publishActionToRabbitMQ("delete-image", imageEntity);
//        rabbit.publishImageToRabbit("delete-image", imageEntity);

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
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
