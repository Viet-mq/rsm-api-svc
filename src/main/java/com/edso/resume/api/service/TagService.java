package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.TagEntity;
import com.edso.resume.api.domain.request.CreateTagRequest;
import com.edso.resume.api.domain.request.DeleteTagRequest;
import com.edso.resume.api.domain.request.UpdateTagRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface TagService {
    GetArrayResponse<TagEntity> findAllTag(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createTag(CreateTagRequest request);

    BaseResponse updateTag(UpdateTagRequest request);

    BaseResponse deleteTag(DeleteTagRequest request);
}
