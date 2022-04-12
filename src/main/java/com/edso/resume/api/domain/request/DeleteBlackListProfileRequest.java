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
public class DeleteBlackListProfileRequest extends BaseAuthRequest {
    private String profileId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(profileId) || profileId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn profile id");
        }
        return null;
    }
}