package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UpdateProfileRequest extends BaseAuthRequest {
    private String id;
    private String fullName;
    private String gender;
    private String phoneNumber;
    private String email;
    private Long dateOfBirth;
    private String hometown;
    private String school;
    private String job;
    private String levelJob;
    private String sourceCV;
    private String hrRef;
    private Long dateOfApply;
    private String talentPool;
    private String department;
    private String levelSchool;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(fullName)) {
            return new BaseResponse(-1, "Vui lòng nhập họ và tên");
        }
        if (fullName.length() > 255 || !validateFullName(fullName)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng họ và tên");
        }
        if (dateOfBirth != null && dateOfBirth < 0) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng ngày sinh");
        }
        if (Strings.isNullOrEmpty(gender)) {
            return new BaseResponse(-1, "Vui lòng nhập giới tính");
        }
        if (!gender.equals("Nam") && !gender.equals("Nữ")) {
            return new BaseResponse(-1, "Vui lòng nhập Nam hoặc Nữ");
        }
        if (!Strings.isNullOrEmpty(hometown) && hometown.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập quê quán ít hơn 255 ký tự");
        }
        if (!Strings.isNullOrEmpty(school) && school.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập tên trường học ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(phoneNumber) || phoneNumber.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập số điện thoại");
        }
        if (!Strings.isNullOrEmpty(levelSchool) && levelSchool.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập trình độ đào tạo ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(email)) {
            return new BaseResponse(-1, "Vui lòng nhập email");
        }
        if (email.length() > 255 || !validateEmail(email)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng email");
        }
        if (!Strings.isNullOrEmpty(job) && job.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập tên công việc ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(levelJob) || levelJob.length() > 255 ) {
            return new BaseResponse(-1, "Vui lòng nhập vị trí ứng tuyển");
        }
        if (Strings.isNullOrEmpty(sourceCV)|| sourceCV.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập nguồn ứng tuyển");
        }
        if (!Strings.isNullOrEmpty(hrRef) && hrRef.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập hr ref ít hơn 255 ký tự");
        }
        if (dateOfApply == null || dateOfApply < 0) {
            return new BaseResponse(-1, "Vui lòng nhập ngày ứng tuyển");
        }
        if (Strings.isNullOrEmpty(talentPool) || talentPool.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập talent pool");
        }
        if (Strings.isNullOrEmpty(department) || department.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập phòng ban");
        }
        return null;
    }

}
