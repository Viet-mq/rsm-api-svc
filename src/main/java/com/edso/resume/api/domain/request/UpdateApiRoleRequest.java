package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UpdateApiRoleRequest extends BaseAuthRequest {

    private String id;
    private String name;
    private String description;
    private List<String> apis;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập name");
        }
        if (Strings.isNullOrEmpty(description)) {
            return new BaseResponse(ErrorCodeDefs.DESCRIPTION, "Vui lòng nhập description");
        }
        if (apis == null || apis.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.APIS, "Vui lòng nhập api id");
        }
        return null;
    }
}
