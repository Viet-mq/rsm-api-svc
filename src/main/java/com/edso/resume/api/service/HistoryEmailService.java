package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;

public interface HistoryEmailService {
    void createHistoryEmail(HistoryEmail historyEmail, HeaderInfo info);

    void deleteHistoryEmail(String idProfile);

    GetArrayResponse<HistoryEmail> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size);
}
