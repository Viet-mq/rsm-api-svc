package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.RecruitmentEntity;
import com.edso.resume.api.domain.request.CreateRecruitmentRequest;
import com.edso.resume.api.domain.request.DeleteRecruitmentRequest;
import com.edso.resume.api.domain.request.DeleteStatusCVRecruitmentRequest;
import com.edso.resume.api.domain.request.UpdateRecruitmentRequest;
import com.edso.resume.api.domain.response.GetRecruitmentResponse;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface RecruitmentService {
    GetArrayResponse<RecruitmentEntity> findAll(HeaderInfo info, Integer page, Integer size, String id, String department, String key, String keySearch, Long from, Long to, String status);

    GetRecruitmentResponse<RecruitmentEntity> findOne(HeaderInfo info, String recruitmentId);

    BaseResponse createRecruitment(CreateRecruitmentRequest request);

    BaseResponse updateRecruitment(UpdateRecruitmentRequest request);

    BaseResponse deleteRecruitment(DeleteRecruitmentRequest request);

    BaseResponse deleteStatusCVRecruitment(DeleteStatusCVRecruitmentRequest request);

}
