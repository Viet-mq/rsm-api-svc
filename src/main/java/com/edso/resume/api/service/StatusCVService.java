package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.StatusCVEntity;
import com.edso.resume.api.domain.request.CreateStatusCVRequest;
import com.edso.resume.api.domain.request.DeleteStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateAllStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateStatusCVRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

import java.util.List;

public interface StatusCVService {

    GetArrayResponse<StatusCVEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    //RES-293
    GetArrayResponse<StatusCVEntity> findAllStatusCVForRecruitment(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createStatusCV(CreateStatusCVRequest request, List<String> children);

    BaseResponse updateStatusCV(UpdateStatusCVRequest request, List<String> children);

    BaseResponse updateAllStatusCV(UpdateAllStatusCVRequest request);

    BaseResponse deleteStatusCV(DeleteStatusCVRequest request);
}
