package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.SkillEntity;
import com.edso.resume.api.domain.request.CreateSkillRequest;
import com.edso.resume.api.domain.request.DeleteSkillRequest;
import com.edso.resume.api.domain.request.UpdateSkillRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface SkillService {
    GetArrayResponse<SkillEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createSkill(CreateSkillRequest request);

    BaseResponse updateSkill(UpdateSkillRequest request);

    BaseResponse deleteSkill(DeleteSkillRequest request);
}
