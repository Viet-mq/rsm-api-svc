package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface HistoryService {

    void createHistory(String idProfile,String type, String action, String by);

    GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size);
}
