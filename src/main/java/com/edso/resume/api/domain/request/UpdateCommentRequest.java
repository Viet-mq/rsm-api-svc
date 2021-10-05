package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateCommentRequest extends BaseAuthRequest {
    private String id;
    private String idProfile;
    private String name;
    private String comment;

    @Override
    public String toString() {
        return "{" +
                "\"type\"=\"Update comment\"" +
                ", \"comment\":{" +
                " \"id\"=\"" + id + '\"' +
                ", \"idProfile\"=\"" + idProfile + '\"' +
                ", \"name\"=" + name +
                ", \"comment\"=\"" + comment + '\"' +
                "} }";
    }

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(-1, "Vui lòng nhập tên người comment");
        }
        if (Strings.isNullOrEmpty(comment)) {
            return new BaseResponse(-1, "Vui lòng nhập comment");
        }
        return null;
    }
}
