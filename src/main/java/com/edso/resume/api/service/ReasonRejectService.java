package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReasonRejectEntity;
import com.edso.resume.api.domain.request.CreateReasonRejectRequest;
import com.edso.resume.api.domain.request.DeleteReasonRejectRequest;
import com.edso.resume.api.domain.request.UpdateReasonRejectRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReasonRejectService {
    GetArrayResponse<ReasonRejectEntity> findAll(HeaderInfo info, Integer page, Integer size);

    BaseResponse createReasonReject(CreateReasonRejectRequest request);

    BaseResponse updateReasonReject(UpdateReasonRejectRequest request);

    BaseResponse deleteReasonReject(DeleteReasonRejectRequest request);
}
