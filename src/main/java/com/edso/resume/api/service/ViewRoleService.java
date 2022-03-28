package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ViewRoleEntity;
import com.edso.resume.api.domain.request.CreateViewRoleRequest;
import com.edso.resume.api.domain.request.DeleteViewRoleRequest;
import com.edso.resume.api.domain.request.UpdateViewRoleRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ViewRoleService {
    GetArrayResponse<ViewRoleEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size);

    BaseResponse createViewRole(CreateViewRoleRequest request);

    BaseResponse updateViewRole(UpdateViewRoleRequest request);

    BaseResponse deleteViewRole(DeleteViewRoleRequest request);
}
