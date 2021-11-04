package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.DeleteImageProfileRequest;
import com.edso.resume.api.domain.request.UpdateImageProfileRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface ImageProfileService {
    BaseResponse updateImageProfile(UpdateImageProfileRequest request);

    BaseResponse deleteImageProfile(DeleteImageProfileRequest request);
}
