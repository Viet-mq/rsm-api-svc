package com.edso.resume.api.service;

import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadProfilesService {

    BaseResponse uploadProfiles(MultipartFile uploadProfilesRequest, HeaderInfo info);
}
