package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.BlacklistEntity;
import com.edso.resume.api.domain.request.CreateBlacklistRequest;
import com.edso.resume.api.domain.request.DeleteBlacklistRequest;
import com.edso.resume.api.domain.request.UpdateBlacklistRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface BlacklistService {
    GetArrayResponse<BlacklistEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createBlacklist(CreateBlacklistRequest request);

    BaseResponse updateBlacklist(UpdateBlacklistRequest request);

    BaseResponse deleteBlacklist(DeleteBlacklistRequest request);
}
