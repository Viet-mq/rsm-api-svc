package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateDepartmentRequest extends BaseAuthRequest {
    private String idCompany;
    private String name;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idCompany)) {
            return new BaseResponse(-1, "Vui lòng nhập id company");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(-1, "Vui lòng nhập tên phòng ban");
        }
        return null;
    }
}
