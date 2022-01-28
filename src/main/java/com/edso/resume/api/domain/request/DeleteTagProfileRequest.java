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
public class DeleteTagProfileRequest extends BaseAuthRequest {

    private String profileId;
    private String tag;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(profileId) || profileId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn profile id");
        }
        if (tag == null || tag.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn tags");
        }
        return null;
    }

}
