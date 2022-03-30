package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.entities.OrganizationEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface OrganizationService {
    GetArrayResponse<OrganizationEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createOrganization(CreateOrganizationRequest request);

    BaseResponse updateOrganization(UpdateOrganizationRequest request);

    BaseResponse deleteOrganization(DeleteOrganizationRequest request);
    
}
