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
public class UpdateRejectProfileRequest extends BaseAuthRequest {
    private String idProfile;
    private String reason;
    private String recruitmentId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 50) {
            return new BaseResponse(ErrorCodeDefs.ID_PROFILE, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(reason)) {
            return new BaseResponse(ErrorCodeDefs.REASON, "Vui lòng nhập lý do loại ứng viên");
        }
        if (Strings.isNullOrEmpty(recruitmentId)) {
            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng nhập recruitment id");
        }
        if (reason.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.REASON, "Vui lòng nhập lý do loại ứng viên ít hơn 255 ký tự");
        }
        return null;
    }
}
