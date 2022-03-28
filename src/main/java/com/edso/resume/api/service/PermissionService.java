package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.PermissionEntity;
import com.edso.resume.api.domain.request.CreatePermissionRequest;
import com.edso.resume.api.domain.request.DeletePermissionRequest;
import com.edso.resume.api.domain.request.UpdatePermissionRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface PermissionService {

    GetArrayResponse<PermissionEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size);

    BaseResponse createPermission(CreatePermissionRequest request);

    BaseResponse updatePermission(UpdatePermissionRequest request);

    BaseResponse deletePermission(DeletePermissionRequest request);
}
