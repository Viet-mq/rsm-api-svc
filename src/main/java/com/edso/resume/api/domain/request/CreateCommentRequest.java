package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateCommentRequest extends BaseAuthRequest {

    private String idProfile;
    private String name;
    private String comment;

    @Override
    public String toString() {
        return "{" +
                "\"type\"=\"Create comment\"" +
                ", \"comment\":{" +
                " \"idProfile\"=\"" + idProfile + '\"' +
                ", \"name\"=" + name +
                ", \"comment\"=\"" + comment + '\"' +
                "} }";
    }

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(-1, "Vui lòng nhập trên người comment");
        }
        if (Strings.isNullOrEmpty(comment)) {
            return new BaseResponse(-1, "Vui lòng nhập comment");
        }
        return null;
    }

}
