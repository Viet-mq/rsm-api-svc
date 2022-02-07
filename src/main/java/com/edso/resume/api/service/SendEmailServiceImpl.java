package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.SendEmailRequest;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class SendEmailServiceImpl extends BaseService implements SendEmailService {

    private final HistoryEmailService historyEmailService;
    private final RabbitMQOnlineActions rabbit;
    @Value("${mail.fileSize}")
    private Long fileSize;

    protected SendEmailServiceImpl(MongoDbOnlineSyncActions db, HistoryEmailService historyEmailService, RabbitMQOnlineActions rabbit) {
        super(db);
        this.historyEmailService = historyEmailService;
        this.rabbit = rabbit;
    }

    @Override
    public BaseResponse sendEmail(SendEmailRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                for (MultipartFile file : request.getFiles()) {
                    if (file != null && file.getSize() > fileSize) {
                        return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
                    }
                }
            }
            List<Document> list = db.findAll(CollectionNameDefs.COLL_PROFILE, Filters.in(DbKeyConfig.ID, request.getProfileIds()), null, 0, 0);
            if (list.size() != request.getProfileIds().size()) {
                response.setFailed("Profile id này không tồn tại");
                return response;
            }

            List<IdEntity> ids = new ArrayList<>();
            for (String profileId : request.getProfileIds()) {
                IdEntity id = IdEntity.builder()
                        .profileId(profileId)
                        .build();
                ids.add(id);
            }
            List<String> paths = historyEmailService.createHistoryEmails(ids, request.getSubject(), request.getContent(), request.getFiles(), request.getInfo());
            rabbit.publishEmails(TypeConfig.EMAILS_CANDIDATE, request, paths, ids);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }
}
