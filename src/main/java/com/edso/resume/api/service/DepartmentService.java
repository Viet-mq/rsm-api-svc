package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.DepartmentEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface DepartmentService {
    GetArrayResponse<DepartmentEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createDepartment(CreateDepartmentRequest request);

    BaseResponse updateDepartment(UpdateDepartmentRequest request);

    BaseResponse deleteDepartment(DeleteDepartmentRequest request);
}
