package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.response.BaseResponse;
import org.springframework.stereotype.Service;

@Service
public class PublishToHomepageServiceImpl extends BaseService implements PublishToHomepageService {
    protected PublishToHomepageServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public BaseResponse publishToHomepage() {
        BaseResponse response = new BaseResponse();
        response.setSuccess();
        return response;
    }
}
