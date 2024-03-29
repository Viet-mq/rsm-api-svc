package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.AppUtils;
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
public class
CreateProfileRequest extends BaseAuthRequest {

    private String fullName;
    private Long dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private String hometown;
    private String levelSchool;
    private String school;
    private Long dateOfApply;
    private String sourceCV;
    private String job;
    private List<String> skill;
    private String levelJob;
    private String talentPool;
    private String hrRef;
    private String mailRef2;
    private String pic;
    private String department;
    private String avatarColor;
    private Long time;
    private String linkedin;
    private String facebook;
    private String skype;
    private String github;
    private String otherTech;
    private String web;
    private String status;
    private String company;
    private String recruitment;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(fullName)) {
            return new BaseResponse(ErrorCodeDefs.FULL_NAME, "Vui lòng nhập họ và tên");
        }
//        if (fullName.length() > 255 || !AppUtils.validateFullName(fullName)) {
//            return new BaseResponse(ErrorCodeDefs.FULL_NAME, "Vui lòng nhập đúng định dạng họ và tên");
//        }
        if (!Strings.isNullOrEmpty(gender)) {
            if (!gender.equals(NameConfig.NAM) && !gender.equals(NameConfig.NU)) {
                return new BaseResponse(ErrorCodeDefs.GENDER, "Vui lòng nhập Nam hoặc Nữ");
            }
        }
        if (!Strings.isNullOrEmpty(hometown) && hometown.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.HOMETOWN, "Vui lòng nhập địa chỉ ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(school) && school.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.SCHOOL, "Vui lòng nhập nơi đào tạo học ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(levelSchool) && levelSchool.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.LEVEL_SCHOOL, "Vui lòng nhập trình độ đào tạo ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(email) && !AppUtils.validateEmail(email.replaceAll(" ", ""))) {
            return new BaseResponse(ErrorCodeDefs.EMAIL, "Vui lòng nhập đúng định dạng email");
        }
        if (!Strings.isNullOrEmpty(mailRef2) && !AppUtils.validateEmail(mailRef2.replaceAll(" ", ""))) {
            return new BaseResponse(ErrorCodeDefs.MAIL_REF2, "Vui lòng nhập đúng định dạng email người giới thiệu");
        }
        if (Strings.isNullOrEmpty(job) || job.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.JOB, "Vui lòng nhập vị trí công việc");
        }
        if (!Strings.isNullOrEmpty(levelJob) && levelJob.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.LEVEL_JOB, "Vui lòng nhập cấp bậc công việc ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(sourceCV) || sourceCV.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.SOURCE_CV, "Vui lòng nhập nguồn ứng viên");
        }
        if (!Strings.isNullOrEmpty(hrRef) && hrRef.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.HR_REF, "Vui lòng nhập tên người giới thiệu ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(talentPool) && talentPool.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.TALENT_POOL, "Vui lòng nhập talent pool ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(department) && department.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.DEPARTMENT, "Vui lòng nhập phòng ban ít hơn 255 ký tự");
        }
        return null;
    }

}
