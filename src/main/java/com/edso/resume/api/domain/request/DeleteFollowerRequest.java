package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeleteFollowerRequest extends BaseAuthRequest{
    private String profileId;
    private String username;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(profileId) || profileId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn profile id");
        }
        if (Strings.isNullOrEmpty(username) || username.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn username");
        }
        return null;
    }
}
