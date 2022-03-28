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
public class UpdateActionRequest extends BaseAuthRequest {

    private String id;
    private String title;
    private String key;
    @JsonProperty("permission_id")
    private String permissionId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(title)) {
            return new BaseResponse(ErrorCodeDefs.TITLE, "Vui lòng nhập title");
        }
        if (Strings.isNullOrEmpty(key)) {
            return new BaseResponse(ErrorCodeDefs.KEY, "Vui lòng nhập key");
        }
        if (Strings.isNullOrEmpty(permissionId)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập permission id");
        }
        return null;
    }
}
