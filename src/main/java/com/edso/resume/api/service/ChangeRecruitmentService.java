package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.ChangeRecruitmentRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface ChangeRecruitmentService {

    BaseResponse changeRecruitment(ChangeRecruitmentRequest request);

}
