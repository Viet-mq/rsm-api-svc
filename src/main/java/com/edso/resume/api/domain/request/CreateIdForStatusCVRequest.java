package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.entities.StatusCV;
import com.edso.resume.api.domain.response.StatusCVResponse;
import com.edso.resume.lib.common.ErrorCodeDefs;
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

    public StatusCVResponse validate() {
        StatusCVResponse response = new StatusCVResponse();
        if (Strings.isNullOrEmpty(name)) {
            response.setResult(ErrorCodeDefs.NAME, "Vui lòng nhập tên vòng tuyển dụng");
            return response;
        }
        if (statusCVS == null || statusCVS.isEmpty()) {
            response.setResult(ErrorCodeDefs.STATUS_CV, "Vui lòng nhập tất cả vòng tuyển dụng");
            return response;
        }
        return null;
    }
}
