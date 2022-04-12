package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.EmailResult;

import java.util.List;

public interface SendEmailService {
    List<EmailResult> sendEmail(
            String profileId,
            List<String> emails,
            String subject,
            String content);

    List<EmailResult> sendCalendarEmail(String calendarId,
                                        List<String> emails,
                                        String subject,
                                        String content);
}
