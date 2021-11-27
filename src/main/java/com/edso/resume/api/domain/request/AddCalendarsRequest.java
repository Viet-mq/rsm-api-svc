package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.common.NameConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class AddCalendarsRequest extends BaseAuthRequest {
    private List<CreateTimeCalendarRequest> times;
    private String recruitmentId;
    private String interviewAddress;
    private String floor;
    private String type;
    private List<String> interviewers;
    private String note;

    public BaseResponse validate() {
        if (times == null || times.isEmpty()) {
            return new BaseResponse(1, "Vui lòng nhập danh sách lịch phỏng vấn");
        }
//        if (Strings.isNullOrEmpty(recruitmentId) || recruitmentId.length() > 255) {
//            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng nhập tin tuyển dụng");
//        }
        if (Strings.isNullOrEmpty(interviewAddress) || interviewAddress.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ADDRESS, "Vui lòng nhập địa điểm");
        }
        if (!Strings.isNullOrEmpty(floor) && floor.length() > 100) {
            return new BaseResponse(ErrorCodeDefs.FLOOR, "Vui lòng nhập tầng (phòng) ít hơn 100 ký tự");
        }
        if (Strings.isNullOrEmpty(type) || type.length() > 100) {
            return new BaseResponse(ErrorCodeDefs.TYPE, "Vui lòng nhập loại phỏng vấn");
        }
        if (!type.equals(NameConfig.PHONG_VAN_TRUC_TIEP) && !type.equals(NameConfig.PHONG_VAN_ONLINE)) {
            return new BaseResponse(ErrorCodeDefs.TYPE, "Vui lòng nhập loại phỏng vấn trực tiếp hoặc online");
        }
        if (!Strings.isNullOrEmpty(note) && note.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NOTE, "Vui lòng nhập ghi chép nội bộ ít hơn 255 ký tự");
        }
        return null;
    }

}
