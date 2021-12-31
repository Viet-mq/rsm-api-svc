package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.SendOutlookCalendarRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface SendOutlookCalendarService {
    BaseResponse sendOutlookCalendar(SendOutlookCalendarRequest request);
}
