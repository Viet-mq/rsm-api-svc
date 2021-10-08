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
    private String idProfile;
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
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        if (time <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian");
        }
        if (Strings.isNullOrEmpty(address)) {
            return new BaseResponse(-1, "Vui lòng nhập địa điểm");
        }
        if (Strings.isNullOrEmpty(form)) {
            return new BaseResponse(-1, "Vui lòng nhập hình thức");
        }
        if (interviewer.size() == 0) {
            return new BaseResponse(-1, "Vui lòng nhập người phỏng vấn");
        }
        if (Strings.isNullOrEmpty(interviewee)) {
            return new BaseResponse(-1, "Vui lòng nhập người tham gia");
        }
        if (Strings.isNullOrEmpty(content)) {
            return new BaseResponse(-1, "Vui lòng nhập nội dung");
        }
        if (Strings.isNullOrEmpty(question)) {
            return new BaseResponse(-1, "Vui lòng nhập câu hỏi");
        }
        if (Strings.isNullOrEmpty(comments)) {
            return new BaseResponse(-1, "Vui lòng nhập nhận xét");
        }
        if (Strings.isNullOrEmpty(evaluation)) {
            return new BaseResponse(-1, "Vui lòng nhập đánh giá");
        }
        if (Strings.isNullOrEmpty(status)) {
            return new BaseResponse(-1, "Vui lòng nhập trạng thái");
        }
        if (Strings.isNullOrEmpty(reason)) {
            return new BaseResponse(-1, "Vui lòng nhập lý do");
        }
        if (timeStart <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian bắt đầu");
        }
        if (timeFinish <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian kết thúc");
        }
        return null;
    }
}
