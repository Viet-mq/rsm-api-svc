package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.response.BaseResponse;

public interface AddCalendarsService {
    BaseResponse addCalendars(AddCalendarsRequest request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate, RelatedPeopleRequest relatedPeople);
}
