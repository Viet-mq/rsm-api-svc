package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UpdateBlacklistRequest extends BaseAuthRequest {
    private String id;
    private String email;
    private String phoneNumber;
    private String ssn;
    private String name;
    private String reason;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(email)) {
            return new BaseResponse(-1, "Vui lòng nhập email");
        }
        if (Strings.isNullOrEmpty(phoneNumber)) {
            return new BaseResponse(-1, "Vui lòng nhập số điện thoại");
        }
        if (Strings.isNullOrEmpty(ssn)) {
            return new BaseResponse(-1, "Vui lòng nhập số CMND, CCCD");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(-1, "Vui lòng nhập tên");
        }
        if (!validateEmail(email)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng email");
        }
        return null;
    }
}
