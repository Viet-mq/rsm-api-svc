package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.RoleEntity;
import com.edso.resume.api.domain.request.CreateRoleRequest;
import com.edso.resume.api.domain.request.DeleteRoleRequest;
import com.edso.resume.api.domain.request.UpdateRoleRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface RoleService {
    GetArrayResponse<RoleEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size);

    BaseResponse createRole(CreateRoleRequest request);

    BaseResponse updateRole(UpdateRoleRequest request);

    BaseResponse deleteRole(DeleteRoleRequest request);
}
