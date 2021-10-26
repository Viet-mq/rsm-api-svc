package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;

public interface HistoryService {

    void createHistoryProfile(String idProfile, String type, String action, String username);

    void createHistoryCalendar(String idProfile, String idCalendar, String type, String action, String username);

    void deleteHistoryProfile(String idProfile);

    void deleteHistoryCalendar(String idCalendar);

    GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size);
}
