package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UpdateCalendarProfileRequest extends BaseAuthRequest {

    private String id;
    private Long time;
    private String address;
    private String form;
    private List<String> interviewer;
    private String interviewee;
    private String content;
    private String question;
    private String comments;
    private String evaluation;
    private String status;
    private String reason;
    private Long timeStart;
    private Long timeFinish;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (time == null || time < 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian");
        }
        if (Strings.isNullOrEmpty(address) || address.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập địa điểm");
        }
        if (Strings.isNullOrEmpty(form) || form.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập hình thức");
        }
        if (interviewer == null || interviewer.isEmpty()) {
            return new BaseResponse(-1, "Vui lòng nhập người phỏng vấn");
        }
        if (Strings.isNullOrEmpty(interviewee) || interviewee.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập người tham gia");
        }
        if (Strings.isNullOrEmpty(content) || content.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập nội dung");
        }
        if (Strings.isNullOrEmpty(question) || question.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập câu hỏi");
        }
        if (Strings.isNullOrEmpty(comments) || comments.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập nhận xét");
        }
        if (Strings.isNullOrEmpty(evaluation) || evaluation.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập đánh giá");
        }
        if (Strings.isNullOrEmpty(status) || status.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập trạng thái");
        }
        if (Strings.isNullOrEmpty(reason) || reason.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập lý do");
        }
        if (timeStart == null || timeStart < 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian bắt đầu");
        }
        if (timeFinish == null || timeFinish < 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian kết thúc");
        }
        return null;
    }
}
