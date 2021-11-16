package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.StatisticalEntity;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;

public interface StatisticalService {
    GetArrayStatisticalReponse<StatisticalEntity> findAll(HeaderInfo info, Long from, Long to, Integer page, Integer size);
}
