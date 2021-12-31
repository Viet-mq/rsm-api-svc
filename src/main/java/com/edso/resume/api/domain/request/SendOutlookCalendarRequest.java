package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class SendOutlookCalendarRequest extends BaseAuthRequest {
    private String subject;
    private String job;
    private String content;
    private Long startTime;
    private Long endTime;
    private String address;
    private List<String> interviewers;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(subject) || subject.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập tiêu đề");
        }
        if (Strings.isNullOrEmpty(job) || job.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập vị trí công việc");
        }
        if (startTime == null || startTime < 0) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập thời gian bắt đầu");
        }
        if (endTime == null || endTime < 0) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập thời gian kết thúc");
        }
        if (Strings.isNullOrEmpty(content)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập nội dung");
        }
        if (Strings.isNullOrEmpty(address)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập địa chỉ");
        }
        if (interviewers == null || interviewers.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập hội đồng tuyển dụng");
        }
        return null;
    }
}
