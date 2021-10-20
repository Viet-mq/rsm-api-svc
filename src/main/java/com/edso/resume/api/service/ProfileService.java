package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetReponse;

public interface ProfileService {

    GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, Integer page, Integer size);

    GetReponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile);

    BaseResponse createProfile(CreateProfileRequest request);

    BaseResponse updateProfile(UpdateProfileRequest request);

    BaseResponse updateDetailProfile(UpdateDetailProfileRequest request);

    BaseResponse deleteProfile(DeleteProfileRequest request);

    BaseResponse updateStatusProfile(UpdateStatusProfileRequest request);

}
