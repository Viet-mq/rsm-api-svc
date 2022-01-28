package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AddTagsProfileRequest extends BaseAuthRequest {
    private String profileId;
    private List<String> tags;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(profileId) || profileId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn profile id");
        }
        if (tags == null || tags.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng chọn tags");
        }
        return null;
    }
}
