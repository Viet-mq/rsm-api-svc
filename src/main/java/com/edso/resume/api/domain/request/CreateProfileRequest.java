package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateProfileRequest extends BaseAuthRequest {
    private String name;
    private String dateOfBirth;

    public BaseResponse validate(){
        return null;
    }

}
