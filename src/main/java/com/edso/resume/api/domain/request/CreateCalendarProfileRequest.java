package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.Object.Comment;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateCalendarProfileRequest extends BaseAuthRequest{
    private String idProfile;
    private Long time;
    private String address;
    private String form;
    private List<String> interviewer;
    private String interviewee;
    private String content;
    private List<String> question;
    private List<Comment> comments;
    private String evaluation;
    private String status;
    private String reason;
    private Long timeStart;
    private Long timeFinish;

    public BaseResponse validate(){
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(time.toString())) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian");
        }
        if (Strings.isNullOrEmpty(address)) {
            return new BaseResponse(-1, "Vui lòng nhập địa điểm");
        }
        if (Strings.isNullOrEmpty(form)) {
            return new BaseResponse(-1, "Vui lòng nhập hình thức");
        }
        if (interviewer.size()==0) {
            return new BaseResponse(-1, "Vui lòng nhập người phỏng vấn");
        }
        if (Strings.isNullOrEmpty(interviewee)) {
            return new BaseResponse(-1, "Vui lòng nhập người tham gia");
        }
        if (Strings.isNullOrEmpty(content)) {
            return new BaseResponse(-1, "Vui lòng nhập nội dung");
        }
        if (question.size()==0) {
            return new BaseResponse(-1, "Vui lòng nhập câu hỏi");
        }
        if (comments.size()==0) {
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
        if (Strings.isNullOrEmpty(timeStart.toString())) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian bắt đầu");
        }
        if (Strings.isNullOrEmpty(timeFinish.toString())) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian kết thúc");
        }
        return null;
    }
}
