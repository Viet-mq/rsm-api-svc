package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.entities.SchoolEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface SchoolService {

    GetArrayResponse<SchoolEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createSchool(CreateSchoolRequest request);

    BaseResponse updateSchool(UpdateSchoolRequest request);

    BaseResponse deleteSchool(DeleteSchoolRequest request);
}
