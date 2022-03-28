package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CreateRoleRequest extends BaseAuthRequest {
    private String name;
    private String description;
    @JsonProperty("view_roles")
    private List<String> viewRoles;
    @JsonProperty("api_roles")
    private List<String> apiRoles;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập name");
        }
        if (Strings.isNullOrEmpty(description)) {
            return new BaseResponse(ErrorCodeDefs.DESCRIPTION, "Vui lòng nhập description");
        }
        if (viewRoles == null || viewRoles.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.VIEW_ROLES, "Vui lòng chọn view role");
        }
        if (apiRoles == null || apiRoles.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.VIEW_ROLES, "Vui lòng chọn api role");
        }
        return null;
    }
}
