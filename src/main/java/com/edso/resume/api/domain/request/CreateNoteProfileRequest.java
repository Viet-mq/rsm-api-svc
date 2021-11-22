package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateNoteProfileRequest extends BaseAuthRequest {

    private String idProfile;
    private String username;
    private String comment;
    private String evaluation;
    private MultipartFile file;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID_PROFILE, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(username) || username.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.USERNAME, "Vui lòng nhập username");
        }
        if (!Strings.isNullOrEmpty(comment) && comment.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.COMMENT, "Vui lòng nhận xét không quá 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(evaluation) && evaluation.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.EVALUATION, "Vui lòng đánh giá không quá 255 ký tự");
        }
        return null;
    }
}
