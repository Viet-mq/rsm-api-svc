package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CreateCrawlerRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface CrawlerService {
    BaseResponse create(CreateCrawlerRequest request);

    BaseResponse update(CreateCrawlerRequest request);
}
