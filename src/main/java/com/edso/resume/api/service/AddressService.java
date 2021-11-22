package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.AddressEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface AddressService {
    GetArrayResponse<AddressEntity> findAll(HeaderInfo info, String name, Integer page, Integer size);

    BaseResponse createAddress(CreateAddressRequest request);

    BaseResponse updateAddress(UpdateAddressRequest request);

    BaseResponse deleteAddress(DeleteAddressRequest request);
}
