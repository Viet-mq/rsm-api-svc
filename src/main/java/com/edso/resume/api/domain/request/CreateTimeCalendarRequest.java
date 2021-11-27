package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreateTimeCalendarRequest extends BaseAuthRequest {
    private String idProfile;
    private Long date;
    private Long interviewTime;
    private String avatarColor;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID_PROFILE, "Vui lòng nhập id profile");
        }
        if (date == null || date <= 0) {
            return new BaseResponse(ErrorCodeDefs.DATE, "Vui lòng nhập thời gian phỏng vấn");
        }
        if (date - System.currentTimeMillis() < 60 * 60 * 1000) {
            return new BaseResponse(ErrorCodeDefs.DATE, "Vui lòng nhập thời gian phỏng vấn lớn hơn giờ hiện tại 1 giờ");
        }
        if (interviewTime == null || interviewTime <= 0) {
            return new BaseResponse(ErrorCodeDefs.TIME, "Vui lòng nhập thời lượng phỏng vấn");
        }
        return null;
    }
}
