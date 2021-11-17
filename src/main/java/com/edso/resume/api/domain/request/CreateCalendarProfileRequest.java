package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateCalendarProfileRequest extends BaseAuthRequest {
    private String idProfile;
    private String title;
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
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID_PROFILE, "Vui lòng nhập id profile");
        }
        if (time == null || time < 0) {
            return new BaseResponse(ErrorCodeDefs.TIME, "Vui lòng nhập thời gian");
        }
        if (Strings.isNullOrEmpty(address) || address.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ADDRESS, "Vui lòng nhập địa điểm");
        }
        if (Strings.isNullOrEmpty(form) || form.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.FORM, "Vui lòng nhập hình thức");
        }
        if (interviewer == null || interviewer.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.INTERVIEWER, "Vui lòng nhập người phỏng vấn");
        }
        if (Strings.isNullOrEmpty(interviewee) || interviewee.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.INTERVIEWEE, "Vui lòng nhập người tham gia");
        }
        if (Strings.isNullOrEmpty(content) || content.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.CONTENT, "Vui lòng nhập nội dung");
        }
        if (Strings.isNullOrEmpty(question) || question.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.QUESTION, "Vui lòng nhập câu hỏi");
        }
        if (Strings.isNullOrEmpty(comments) || comments.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.COMMENTS, "Vui lòng nhập nhận xét");
        }
        if (Strings.isNullOrEmpty(evaluation) || evaluation.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.EVALUATION, "Vui lòng nhập đánh giá");
        }
        if (Strings.isNullOrEmpty(status) || status.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.STATUS, "Vui lòng nhập trạng thái");
        }
        if (Strings.isNullOrEmpty(reason) || reason.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.REASON, "Vui lòng nhập lý do");
        }
        if (timeStart == null || timeStart < 0) {
            return new BaseResponse(ErrorCodeDefs.TIME_START, "Vui lòng nhập thời gian bắt đầu");
        }
        if (timeFinish == null || timeFinish < 0) {
            return new BaseResponse(ErrorCodeDefs.TIME_FINISH, "Vui lòng nhập thời gian kết thúc");
        }
        return null;
    }
}
