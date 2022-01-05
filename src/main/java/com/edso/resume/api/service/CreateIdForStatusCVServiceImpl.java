package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.StatusCV;
import com.edso.resume.api.domain.request.CreateIdForStatusCVRequest;
import com.edso.resume.api.domain.response.StatusCVResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateIdForStatusCVServiceImpl extends BaseService implements CreateIdForStatusCVService {

    protected CreateIdForStatusCVServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public StatusCVResponse createIdForStatusCV(CreateIdForStatusCVRequest request) {
        StatusCVResponse response = new StatusCVResponse();
        try {
            for (StatusCV statusCV : request.getStatusCVS()) {
                if(statusCV == null){
                    response.setFailed("Vui lòng nhập vòng tuyển dụng!");
                    return response;
                }
                if (request.getName().toLowerCase().trim().equals(statusCV.getName().toLowerCase())) {
                    response.setFailed("Vòng tuyển dụng này đã tồn tại!");
                    return response;
                }
            }
            response.setId(UUID.randomUUID().toString());
            response.setName(request.getName());
            response.setIsDragDisabled(false);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }
}
