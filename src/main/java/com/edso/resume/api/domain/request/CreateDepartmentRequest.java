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
public class CreateDepartmentRequest extends BaseAuthRequest {
    private String idCompany;
    private String name;
    private String description;

    public BaseResponse validate() {
//        if (!Strings.isNullOrEmpty(idCompany) || idCompany.length() > 255) {
//            return new BaseResponse(-1, "Vui lòng nhập id company");
//        }
        if (Strings.isNullOrEmpty(name) || name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên phòng ban");
        }
        if (!Strings.isNullOrEmpty(description) && description.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.DESCRIPTION, "Vui lòng nhập mô tả ít hơn 255 ký tự");
        }
        return null;
    }
}
