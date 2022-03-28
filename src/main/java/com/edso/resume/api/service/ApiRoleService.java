package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ApiRoleEntity;
import com.edso.resume.api.domain.request.CreateApiRoleRequest;
import com.edso.resume.api.domain.request.DeleteApiRoleRequest;
import com.edso.resume.api.domain.request.UpdateApiRoleRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ApiRoleService {

    GetArrayResponse<ApiRoleEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size);

    BaseResponse createApiRole(CreateApiRoleRequest request);

    BaseResponse updateApiRole(UpdateApiRoleRequest request);

    BaseResponse deleteApiRole(DeleteApiRoleRequest request);
}
