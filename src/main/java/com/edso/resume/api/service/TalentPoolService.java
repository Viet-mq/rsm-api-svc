package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.api.domain.request.CreateTalentPoolRequest;
import com.edso.resume.api.domain.request.DeleteTalentPoolRequest;
import com.edso.resume.api.domain.request.UpdateTalentPoolRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface TalentPoolService {
    GetArrayResponse<TalentPoolEntity> findAll(HeaderInfo headerInfo, String name, Integer page, Integer size);

    BaseResponse createTalentPool(CreateTalentPoolRequest request);

    BaseResponse updateTalentPool(UpdateTalentPoolRequest request);

    BaseResponse deleteTalentPool(DeleteTalentPoolRequest request);
}
