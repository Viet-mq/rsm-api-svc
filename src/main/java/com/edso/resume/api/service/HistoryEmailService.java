package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.api.domain.entities.HistoryEmailEntity;
import com.edso.resume.api.domain.entities.HistoryEmails;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;

import java.util.List;

public interface HistoryEmailService {
    List<String> createHistoryEmail(HistoryEmail historyEmail, HeaderInfo info);

    List<String> createHistoryEmails(HistoryEmails historyEmails, HeaderInfo info);

    void deleteHistoryEmail(String idProfile);

    GetArrayResponse<HistoryEmailEntity> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size);
}
