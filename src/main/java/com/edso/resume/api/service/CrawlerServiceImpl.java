package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.request.CreateCrawlerRequest;
import com.edso.resume.lib.response.BaseResponse;
import org.springframework.stereotype.Service;

@Service
public class CrawlerServiceImpl extends BaseService implements CrawlerService {

    public CrawlerServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public BaseResponse create(CreateCrawlerRequest request) {
        return null;
    }

    @Override
    public BaseResponse update(CreateCrawlerRequest request) {
        return null;
    }

}
