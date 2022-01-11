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
public class ChangeRecruitmentRequest extends BaseAuthRequest {

    private String idProfile;
    private String recruitmentId;
    private String statusCVId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(ErrorCodeDefs.ID_PROFILE, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(recruitmentId)) {
            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng nhập tin tuyển dụng");
        }
        if (Strings.isNullOrEmpty(statusCVId)) {
            return new BaseResponse(ErrorCodeDefs.STATUS_CV, "Vui lòng nhập vòng tuyển dụng");
        }
        return null;
    }
}
