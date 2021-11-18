package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.common.NameConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreateCalendarProfileRequest2 extends BaseAuthRequest{

    private String idProfile;
    private String recruitmentId;
    private Long date;
    private Integer interviewTime;
    private String interviewAddress;
    private String floor;
    private String type;
    private List<String> interviewers;
    private String note;
//    private String sendEmailToInterviewee;
//    private String sendEmailToInterviewer;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID_PROFILE, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(recruitmentId) || recruitmentId.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng nhập tin tuyển dụng");
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
//        if (!Strings.isNullOrEmpty(sendEmailToInterviewee) && sendEmailToInterviewee.length() > 1) {
//            return new BaseResponse(ErrorCodeDefs.SEND_EMAIL_TO_INTERVIEWEE, "Vui lòng nhập gửi email cho ứng viên ít hơn 1 ký tự");
//        }
//        if (!Strings.isNullOrEmpty(sendEmailToInterviewer) && sendEmailToInterviewer.length() > 1) {
//            return new BaseResponse(ErrorCodeDefs.SEND_EMAIL_TO_INTERVIEWER, "Vui lòng nhập gửi email cho hội đồng tuyển dụng ít hơn 1 ký tự");
//        }
        return null;
    }
}
