package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeleteActionRequest extends BaseAuthRequest {

    private String id;
    @JsonProperty("permission_id")
    private String permissionId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(permissionId)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập permission id");
        }
        return null;
    }
}
