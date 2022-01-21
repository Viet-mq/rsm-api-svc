package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CalendarEntity2;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarResponse;

public interface CalendarService2 {
    GetArrayCalendarResponse<CalendarEntity2> findAllCalendar(HeaderInfo info, String idProfile, String key, String keySearch, String recruitment);

    BaseResponse createCalendarProfile(CreateCalendarProfileRequest2 request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate);

    BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest2 request, PresenterRequest presenter, RecruitmentCouncilRequest recruitmentCouncil, CandidateRequest candidate);

    BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request);

    void deleteCalendarByIdProfile(String idProfile);

    void alarmInterview();
}
