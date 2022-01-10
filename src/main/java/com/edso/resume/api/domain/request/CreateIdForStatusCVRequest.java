package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.entities.StatusCV;
import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreateIdForStatusCVRequest extends BaseAuthRequest {
    private List<StatusCV> statusCVS;
    private String name;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên vòng tuyển dụng");
        }
        if (statusCVS == null || statusCVS.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.STATUS_CV, "Vui lòng nhập tất cả vòng tuyển dụng");
        }
        return null;
    }
}
