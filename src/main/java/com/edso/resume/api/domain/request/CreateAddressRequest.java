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
public class CreateAddressRequest extends BaseAuthRequest {
    private String name;
    private String officeName;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(officeName)) {
            return new BaseResponse(ErrorCodeDefs.OFFICE_NAME, "Vui lòng nhập tên văn phòng");
        }
        if (officeName.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.OFFICE_NAME, "Vui lòng nhập tên văn phòng ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập địa chỉ");
        }
        if (name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập địa chỉ ít hơn 255 ký tự");
        }
        return null;
    }
}
