package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.JobLevelEntity;
import com.edso.resume.api.domain.request.CreateJobLevelRequest;
import com.edso.resume.api.domain.request.DeleteJobLevelRequest;
import com.edso.resume.api.domain.request.UpdateJobLevelRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface JobLevelService {
    GetArrayResponse<JobLevelEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createJobLevel(CreateJobLevelRequest request);

    BaseResponse updateJobLevel(UpdateJobLevelRequest request);

    BaseResponse deleteJobLevel(DeleteJobLevelRequest request);
}

