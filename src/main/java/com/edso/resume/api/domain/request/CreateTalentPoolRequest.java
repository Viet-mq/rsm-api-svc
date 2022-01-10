package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateTalentPoolRequest extends BaseAuthRequest {

    private String name;
    private List<String> managers;
    private String description;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(name) || name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên Talent Pool");
        }
        if (managers == null || managers.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.MANAGERS, "Vui lòng nhập người quản lý Talent Pool");
        }
        if(!Strings.isNullOrEmpty(description) && description.length() > 255){
            return new BaseResponse(ErrorCodeDefs.DESCRIPTION, "Vui lòng nhập mô tả ít hơn 255 ký tự");
        }
        return null;
    }
}
