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
public class DeleteStatusCVRecruitmentRequest extends BaseAuthRequest {

    private String statusCVId;
    private String recruitmentId;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(statusCVId) || statusCVId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.STATUS_CV, "Vui lòng chọn status cv id");
        }
        if (Strings.isNullOrEmpty(recruitmentId) || recruitmentId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng chọn recruitment id");
        }
        return null;
    }
}
