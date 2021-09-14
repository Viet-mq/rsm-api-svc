package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CreateProfileRequest;
import com.edso.resume.lib.response.BaseResponse;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {
    @Override
    public BaseResponse createProfile(CreateProfileRequest request) {
        return null;
    }
}
