package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CreateProfileRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface ProfileService {
    BaseResponse createProfile(CreateProfileRequest request);
}
