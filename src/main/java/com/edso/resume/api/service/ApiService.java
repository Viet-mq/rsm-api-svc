package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ApiEntity;
import com.edso.resume.api.domain.request.CreateApiRequest;
import com.edso.resume.api.domain.request.DeleteApiRequest;
import com.edso.resume.api.domain.request.UpdateApiRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ApiService {
    GetArrayResponse<ApiEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size);

    BaseResponse createApi(CreateApiRequest request);

    BaseResponse updateApi(UpdateApiRequest request);

    BaseResponse deleteApi(DeleteApiRequest request);
}
