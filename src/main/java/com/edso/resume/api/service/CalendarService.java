package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest;
import com.edso.resume.api.domain.request.DeleteCalendarProfileRequest;
import com.edso.resume.api.domain.request.UpdateCalendarProfileRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarResponse;

public interface CalendarService {

    GetArrayCalendarResponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile);

    BaseResponse createCalendarProfile(CreateCalendarProfileRequest request);

    BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest request);

    BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request);

    void deleteCalendarByIdProfile(String idProfile);

    void alarmInterview();
}
