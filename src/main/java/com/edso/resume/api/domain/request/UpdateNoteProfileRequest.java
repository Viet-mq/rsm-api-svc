package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
public class UpdateNoteProfileRequest extends BaseAuthRequest {
    private String id;
    private String username;
    private String comment;
    private String evaluation;
    private MultipartFile file;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(username) || username.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.USERNAME, "Vui lòng nhập username");
        }
        if (!Strings.isNullOrEmpty(comment) && comment.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.CONTENT, "Vui lòng nhận xét không quá 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(evaluation) && evaluation.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.EVALUATION, "Vui lòng đánh giá không quá 255 ký tự");
        }
        return null;
    }
}
