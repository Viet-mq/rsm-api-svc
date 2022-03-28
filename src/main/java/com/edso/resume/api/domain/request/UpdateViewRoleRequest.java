package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.entities.Permission;
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
public class UpdateViewRoleRequest extends BaseAuthRequest {
    private String id;
    private String name;
    private String description;
    private List<Permission> permissions;

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
        if (permissions == null || permissions.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.PERMISSIONS, "Vui lòng chọn permission");
        }
        return null;
    }
}
