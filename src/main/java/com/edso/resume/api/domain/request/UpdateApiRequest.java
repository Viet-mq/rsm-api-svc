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
public class UpdateApiRequest extends BaseAuthRequest {
    private String id;
    private String name;
    private String method;
    private String path;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập name");
        }
        if (Strings.isNullOrEmpty(method)) {
            return new BaseResponse(ErrorCodeDefs.METHOD, "Vui lòng nhập method");
        }
        if (Strings.isNullOrEmpty(path)) {
            return new BaseResponse(ErrorCodeDefs.PATH, "Vui lòng nhập path");
        }
        return null;
    }
}
