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
public class CreateSourceCVRequest extends BaseAuthRequest {

    private String name;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(name) || name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên nguồn CV");
        }
        return null;
    }
}
