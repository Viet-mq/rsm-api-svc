package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CandidateRequest;
import com.edso.resume.api.domain.request.ChangeRecruitmentRequest;
import com.edso.resume.api.domain.request.PresenterRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface ChangeRecruitmentService {

    BaseResponse changeRecruitment(ChangeRecruitmentRequest request, CandidateRequest candidate, PresenterRequest presenter);

}
