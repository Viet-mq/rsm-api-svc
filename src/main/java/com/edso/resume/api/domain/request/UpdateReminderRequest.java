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
public class UpdateReminderRequest extends BaseAuthRequest {
    private String id;
    private String title;
    private Long start;
    private Long end;
    private String desc;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(title)) {
            return new BaseResponse(ErrorCodeDefs.CONTENT, "Vui lòng nhập tiêu đề");
        }
        if (title.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.CONTENT, "Vui lòng nhập tiêu đề ít hơn 255 ký tự");
        }
        if (start == null || start < 0) {
            return new BaseResponse(ErrorCodeDefs.TIME, "Vui lòng nhập thời gian bắt đầu");
        }
        if (end == null || end < 0) {
            return new BaseResponse(ErrorCodeDefs.TIME, "Vui lòng nhập thời gian thời gian kết thúc");
        }
        return null;
    }
}
