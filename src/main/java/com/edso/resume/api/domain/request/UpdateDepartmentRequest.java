package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UpdateDepartmentRequest extends BaseAuthRequest {
    private String id;
    private String name;
    private String description;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(name) || name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên phòng ban");
        }
        if (!Strings.isNullOrEmpty(description) && description.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.DESCRIPTION, "Vui lòng nhập mô tả ít hơn 255 ký tự");
        }
        return null;
    }
}
