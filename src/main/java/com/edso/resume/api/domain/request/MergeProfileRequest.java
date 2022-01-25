package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MergeProfileRequest extends BaseAuthRequest {
    private String idProfile;
    private String otherIdProfile;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn id profile được gộp");
        }
        if (Strings.isNullOrEmpty(otherIdProfile) || otherIdProfile.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn id profile bị gộp");
        }
        return null;
    }

}
