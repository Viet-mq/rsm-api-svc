package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteProfileRequest extends BaseAuthRequest {
    private String id;

    @Override
    public String toString() {
        return "{" +
                "\"type\"=\"Delete\"" +
                ", \"profile\":{" +
                " \"id\"=\"" + id + '\"' +
                " } }";
    }

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(-1, "Vui lòng chọn id");
        }
        return null;
    }
}
