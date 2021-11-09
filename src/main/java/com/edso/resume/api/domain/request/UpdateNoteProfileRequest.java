package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
public class UpdateNoteProfileRequest extends BaseAuthRequest {
    private String id;
    private String username;
    private String comment;
    private String evaluation;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(username) || username.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập username");
        }
        if (!Strings.isNullOrEmpty(comment) && comment.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhận xét không quá 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(evaluation) && evaluation.length() > 255) {
            return new BaseResponse(-1, "Vui lòng đánh giá không quá 255 ký tự");
        }
        return null;
    }
}
