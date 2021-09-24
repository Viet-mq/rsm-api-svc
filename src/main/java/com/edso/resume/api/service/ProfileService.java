package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

import java.io.IOException;

public interface ProfileService {

    GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, Integer page, Integer size);

    BaseResponse createProfile(CreateProfileRequest request);

    BaseResponse updateProfile(UpdateProfileRequest request);

    BaseResponse deleteProfile(DeleteProfileRequest request);

    BaseResponse updateStatusProfile(UpdateStatusProfileRequest request);

    byte[] exportExcel(HeaderInfo info, String name) throws IOException;
}
