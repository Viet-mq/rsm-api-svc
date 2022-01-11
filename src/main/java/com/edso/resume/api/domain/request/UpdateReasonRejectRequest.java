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
public class UpdateReasonRejectRequest extends BaseAuthRequest {
    private String id;
    private String reason;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(reason)) {
            return new BaseResponse(ErrorCodeDefs.REASON, "Vui lòng nhập lý do loại ứng viên");
        }
        if (reason.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.REASON, "Vui lòng nhập lý do loại ứng viên ít hơn 255 ký tự");
        }
        return null;
    }
}
