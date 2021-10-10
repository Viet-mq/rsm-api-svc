package com.edso.resume.api.domain.request;

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
    private String SSN;
    private String name;
    private String reason;

    public BaseResponse validate(){
        if (Strings.isNullOrEmpty(email)){
            return new BaseResponse(-1, "Vui lòng nhập blacklist email");
        }
        if (!validateEmail(email)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng email");
        }
        if (Strings.isNullOrEmpty(name)){
            return new BaseResponse(-1, "Vui lòng nhập tên blacklist");
        }
        if(!Strings.isNullOrEmpty(phoneNumber) && !validatePhoneNumber(phoneNumber)){
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng số điện thoại");
        }
        if(!Strings.isNullOrEmpty(SSN) && !validateSSN(SSN)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng SSN");
        }

        return null;
    }
}
