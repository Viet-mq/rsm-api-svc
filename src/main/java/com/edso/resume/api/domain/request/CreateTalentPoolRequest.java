package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateTalentPoolRequest extends BaseAuthRequest{

    private String name;
    private List<String> managers;
    private String description;

    public BaseResponse validate(){
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(-1, "Vui lòng nhập tên Talent Pool");
        }
        if (managers.size() == 0) {
            return new BaseResponse(-1, "Vui lòng nhập người quản lý Talent Pool");
        }

        return null;
    }
}
