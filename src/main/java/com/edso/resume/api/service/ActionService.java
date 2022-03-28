package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CreateActionRequest;
import com.edso.resume.api.domain.request.DeleteActionRequest;
import com.edso.resume.api.domain.request.UpdateActionRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface ActionService {

//    GetArrayResponse<ActionEntity> findAll(HeaderInfo info, String id, String name, Integer page, Integer size);

    BaseResponse createAction(CreateActionRequest request);

    BaseResponse updateAction(UpdateActionRequest request);

    BaseResponse deleteAction(DeleteActionRequest request);
}
