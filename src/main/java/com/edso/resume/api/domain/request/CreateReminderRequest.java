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
public class CreateReminderRequest extends BaseAuthRequest {
    private String content;
    private Long time;
    private String repeat;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(content)) {
            return new BaseResponse(ErrorCodeDefs.CONTENT, "Vui lòng nhập nội dung");
        }
        if (content.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.CONTENT, "Vui lòng nhập nội dung ít hơn 255 ký tự");
        }
        if (time == null || time < 0) {
            return new BaseResponse(ErrorCodeDefs.TIME, "Vui lòng nhập thời gian");
        }
        if (Strings.isNullOrEmpty(repeat) || repeat.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập lặp lại");
        }
        return null;
    }
}
