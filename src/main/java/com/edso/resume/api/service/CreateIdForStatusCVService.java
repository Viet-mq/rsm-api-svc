package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CreateIdForStatusCVRequest;
import com.edso.resume.api.domain.response.StatusCVResponse;

public interface CreateIdForStatusCVService {
    StatusCVResponse createIdForStatusCV(CreateIdForStatusCVRequest request);
}
