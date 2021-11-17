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
public class CreateProfileRequest extends BaseAuthRequest {

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
    private String recruitment;
    private String talentPool;
    private String hrRef;
    private String mailRef;
    private String department;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(fullName)) {
            return new BaseResponse(ErrorCodeDefs.FULL_NAME, "Vui lòng nhập họ và tên");
        }
        if (fullName.length() > 255 || !validateFullName(fullName)) {
            return new BaseResponse(ErrorCodeDefs.FULL_NAME, "Vui lòng nhập đúng định dạng họ và tên");
        }
        if (dateOfBirth != null && dateOfBirth < 0) {
            return new BaseResponse(ErrorCodeDefs.DATE_OF_BIRTH, "Vui lòng nhập đúng định dạng ngày sinh");
        }
        if (Strings.isNullOrEmpty(gender)) {
            return new BaseResponse(ErrorCodeDefs.GENDER, "Vui lòng nhập giới tính");
        }
        if (!gender.equals(NameConfig.NAM) && !gender.equals(NameConfig.NU)) {
            return new BaseResponse(ErrorCodeDefs.GENDER, "Vui lòng nhập Nam hoặc Nữ");
        }
        if (!Strings.isNullOrEmpty(hometown) && hometown.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.HOMETOWN, "Vui lòng nhập địa chỉ ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(school) && school.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.SCHOOL, "Vui lòng nhập nơi đào tạo học ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(phoneNumber) && !validatePhoneNumber(phoneNumber)) {
            return new BaseResponse(ErrorCodeDefs.PHONE_NUMBER, "Vui lòng nhập số điện thoại đúng định dạng");
        }
        if (!Strings.isNullOrEmpty(levelSchool) && levelSchool.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.LEVEL_SCHOOL, "Vui lòng nhập trình độ đào tạo ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(email)) {
            return new BaseResponse(ErrorCodeDefs.EMAIL, "Vui lòng nhập email");
        }
        if (email.length() > 255 || !validateEmail(email)) {
            return new BaseResponse(ErrorCodeDefs.EMAIL, "Vui lòng nhập đúng định dạng email");
        }
        if (Strings.isNullOrEmpty(job) || job.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.JOB, "Vui lòng nhập vị trí công việc");
        }
        if (!Strings.isNullOrEmpty(levelJob) && levelJob.length() > 255 ) {
            return new BaseResponse(ErrorCodeDefs.LEVEL_JOB, "Vui lòng nhập cấp bậc công việc ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(sourceCV)|| sourceCV.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.SOURCE_CV, "Vui lòng nhập nguồn ứng viên");
        }
        if (!Strings.isNullOrEmpty(hrRef) && hrRef.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.HR_REF, "Vui lòng nhập tên người giới thiệu ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(mailRef)) {
            if (mailRef.length() > 255 || !validateEmail(mailRef)) {
                return new BaseResponse(ErrorCodeDefs.MAIL_REF, "Vui lòng nhập đúng định dạng email");
            }
        }
        if (dateOfApply == null || dateOfApply < 0) {
            return new BaseResponse(ErrorCodeDefs.DATE_OF_APPLY, "Vui lòng nhập ngày ứng tuyển");
        }
        if (!Strings.isNullOrEmpty(talentPool) && talentPool.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.TALENT_POOL, "Vui lòng nhập talent pool ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(department) && department.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.DEPARTMENT, "Vui lòng nhập phòng ban ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(recruitment) && recruitment.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.RECRUITMENT, "Vui lòng nhập tin tuyển dụng ít hơn 255 ký tự");
        }
        return null;
    }

}
