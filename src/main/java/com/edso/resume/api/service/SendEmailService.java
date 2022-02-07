package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.SendEmailRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface SendEmailService {
    BaseResponse sendEmail(SendEmailRequest request);
}
