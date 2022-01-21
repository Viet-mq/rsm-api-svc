package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.AddCalendarsRequest;
import com.edso.resume.api.domain.request.CandidateRequest;
import com.edso.resume.api.domain.request.PresenterRequest;
import com.edso.resume.api.domain.request.RecruitmentCouncilRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface AddCalendarsService {
    BaseResponse addCalendars(AddCalendarsRequest request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate);
}
