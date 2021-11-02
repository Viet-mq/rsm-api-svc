package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetResponse;

public interface ProfileService {

    GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, String talentPool, Integer page, Integer size);

    GetResponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile);

    BaseResponse createProfile(CreateProfileRequest request);

    BaseResponse updateProfile(UpdateProfileRequest request);

    BaseResponse updateDetailProfile(UpdateDetailProfileRequest request);

    BaseResponse deleteProfile(DeleteProfileRequest request);

    BaseResponse updateStatusProfile(UpdateStatusProfileRequest request);

}
