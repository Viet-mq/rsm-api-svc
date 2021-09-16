package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.entities.SourceCVEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface SourceCVService {

    GetArrayResponse<SourceCVEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createSourceCV(CreateSourceCVRequest request);

    BaseResponse updateSourceCV(UpdateSourceCVRequest request);

    BaseResponse deleteSourceCV(DeleteSourceCVRequest request);
}
