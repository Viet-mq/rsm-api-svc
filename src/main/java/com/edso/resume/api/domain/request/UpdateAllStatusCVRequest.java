package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.entities.StatusCV;
import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateAllStatusCVRequest extends BaseAuthRequest {
    private List<StatusCV> statusCVS;

    public BaseResponse validate() {
        if (statusCVS == null || statusCVS.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.STATUS_CV, "Vui lòng nhập tất cả vòng tuyển dụng");
        }
        return null;
    }
}
