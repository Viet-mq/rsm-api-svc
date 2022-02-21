package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateBlacklistRequest extends BaseAuthRequest {
    private String email;
    private String phoneNumber;
    private String ssn;
    private String name;
    private String reason;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(email)) {
            return new BaseResponse(ErrorCodeDefs.EMAIL, "Vui lòng nhập blacklist email");
        }
        if (email.length() > 255 || !AppUtils.validateEmail(email.trim())) {
            return new BaseResponse(ErrorCodeDefs.EMAIL, "Vui lòng nhập đúng định dạng email");
        }
        if (Strings.isNullOrEmpty(name) || name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập họ và tên");
        }
        if (!Strings.isNullOrEmpty(phoneNumber) && !AppUtils.validatePhone(phoneNumber.trim())) {
            return new BaseResponse(ErrorCodeDefs.PHONE_NUMBER, "Vui lòng nhập đúng định dạng số điện thoại");
        }
        if (!Strings.isNullOrEmpty(ssn) && !validateSSN(ssn)) {
            return new BaseResponse(ErrorCodeDefs.SSN, "Vui lòng nhập đúng định dạng SSN");
        }
        if (!Strings.isNullOrEmpty(reason) && reason.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.REASON, "Vui lòng nhập lý do ít hơn 255 ký tự");
        }
        return null;
    }
}
