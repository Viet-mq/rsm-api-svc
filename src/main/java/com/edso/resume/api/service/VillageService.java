package com.edso.resume.api.service;


import com.edso.resume.api.domain.entities.VillageEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface VillageService {
    GetArrayResponse<VillageEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createVillage(CreateVillageRequest request);

    BaseResponse updateVillage(UpdateVillageRequest request);

    BaseResponse deleteVillage(DeleteVillageRequest request);
}

