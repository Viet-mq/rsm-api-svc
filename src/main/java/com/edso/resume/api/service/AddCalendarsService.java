package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.AddCalendarsRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface AddCalendarsService {
    BaseResponse addCalendars(AddCalendarsRequest request);
}
