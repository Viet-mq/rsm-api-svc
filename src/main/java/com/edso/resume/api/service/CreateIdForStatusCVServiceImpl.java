package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.response.StatusCVResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateIdForStatusCVServiceImpl extends BaseService implements CreateIdForStatusCVService{

    protected CreateIdForStatusCVServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public StatusCVResponse createIdForStatusCV(String name) {
        StatusCVResponse response = new StatusCVResponse();
        try{
            response.setId(UUID.randomUUID().toString());
            response.setName(name);
            response.setSuccess();
            return response;
        }catch (Throwable ex){
            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }
}
