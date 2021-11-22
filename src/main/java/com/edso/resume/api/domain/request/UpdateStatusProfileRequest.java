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
public class UpdateStatusProfileRequest extends BaseAuthRequest {

    private String id;
    private String recruitmentId;
    private String statusCV;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(recruitmentId) || recruitmentId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng nhập id recruitment");
        }
        if (Strings.isNullOrEmpty(statusCV) || statusCV.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.STATUS_CV, "Vui lòng nhập trạng thái cv");
        }
        return null;
    }
}
