package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.RecruitmentEntity;
import com.edso.resume.api.domain.request.CreateRecruitmentRequest;
import com.edso.resume.api.domain.request.DeleteRecruitmentRequest;
import com.edso.resume.api.domain.request.UpdateRecruitmentRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface RecruitmentService {
    GetArrayResponse<RecruitmentEntity> findAll(HeaderInfo info, Integer page, Integer size);

    BaseResponse createRecruitment(CreateRecruitmentRequest request);

    BaseResponse updateRecruitment(UpdateRecruitmentRequest request);

    BaseResponse deleteRecruitment(DeleteRecruitmentRequest request);
}
