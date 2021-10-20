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
public class UpdateTalentPoolRequest extends BaseAuthRequest{
    private String id;
    private String name;
    private String description;
    private int numberOfProfile;
    private List<String> managers;

    public BaseResponse validate(){
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(-1, "Vui lòng nhập tên Talent Pool");
        }
        if(numberOfProfile <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập số lượng profile");
        }
        if(managers == null || managers.isEmpty()) {
            return new BaseResponse(-1, "Vui lòng nhập tên người quản lý Talent Pool");
        }
        if(Strings.isNullOrEmpty(description)) {
            return new BaseResponse(-1, "Vui lòng nhập mô tả Talent Pool");
        }

        return null;
    }
}
