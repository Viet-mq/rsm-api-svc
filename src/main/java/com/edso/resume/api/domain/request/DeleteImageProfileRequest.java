package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class DeleteImageProfileRequest extends BaseAuthRequest{
    private String idProfile;

    public BaseResponse validate(){
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        return null;
    }
}
