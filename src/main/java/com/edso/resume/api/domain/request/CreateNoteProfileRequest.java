package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateNoteProfileRequest extends BaseAuthRequest {

    private String idProfile;
    private String username;
    private String comment;
    private String evaluation;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(username)) {
            return new BaseResponse(-1, "Vui lòng nhập username");
        }
        return null;
    }
}
