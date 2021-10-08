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
        if (Strings.isNullOrEmpty(name)){
            return new BaseResponse(-1, "Vui lòng nhập tên blacklist");
        }

        return null;
    }
}
