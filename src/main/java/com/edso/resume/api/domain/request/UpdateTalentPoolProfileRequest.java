package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateTalentPoolProfileRequest extends BaseAuthRequest {
    private String profileId;
    private String oldTalentPoolId;
    private String newTalentPoolId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(profileId) || profileId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(newTalentPoolId) || newTalentPoolId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập talent pool id mới");
        }
        return null;
    }
}
