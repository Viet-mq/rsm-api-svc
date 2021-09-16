package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.request.CreateJobRequest;
import com.edso.resume.api.domain.request.DeleteJobRequest;
import com.edso.resume.api.domain.request.UpdateJobRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface JobService {

    GetArrayResponse<CategoryEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createJob(CreateJobRequest request);

    BaseResponse updateJob(UpdateJobRequest request);

    BaseResponse deleteJob(DeleteJobRequest request);
}
