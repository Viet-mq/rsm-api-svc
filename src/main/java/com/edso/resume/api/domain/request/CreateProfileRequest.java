package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateProfileRequest extends BaseAuthRequest {

    private String fullName;
    private String gender;
    private Long dateOfBirth;
    private String hometown;
    private String school;
    private String levelSchool;
    private String phoneNumber;
    private String email;
    private String job;
    private String levelJob;
    private String sourceCV;
    private String hrRef;
    private Long dateOfApply;
    private String talentPool;
    private String department;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(fullName)) {
            return new BaseResponse(-1, "Vui lòng nhập họ và tên");
        }
        if (!validateFullName(fullName)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng họ và tên");
        }
        if (Strings.isNullOrEmpty(gender)) {
            return new BaseResponse(-1, "Vui lòng nhập giới tính");
        }
        if (!gender.equals("Nam") && !gender.equals("Nữ")) {
            return new BaseResponse(-1, "Vui lòng nhập Nam hoặc Nữ");
        }
        if (Strings.isNullOrEmpty(phoneNumber)) {
            return new BaseResponse(-1, "Vui lòng nhập số điện thoại");
        }
        if (Strings.isNullOrEmpty(email)) {
            return new BaseResponse(-1, "Vui lòng nhập email");
        }
        if (!validateEmail(email)) {
            return new BaseResponse(-1, "Vui lòng nhập đúng định dạng email");
        }
        if (Strings.isNullOrEmpty(levelJob)) {
            return new BaseResponse(-1, "Vui lòng nhập vị trí ứng tuyển");
        }
        if (Strings.isNullOrEmpty(sourceCV)) {
            return new BaseResponse(-1, "Vui lòng nhập nguồn ứng tuyển");
        }
        if (dateOfApply <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập ngày ứng tuyển");
        }
        if (Strings.isNullOrEmpty(talentPool)) {
            return new BaseResponse(-1, "Vui lòng nhập nguồn ứng tuyển");
        }
        if (Strings.isNullOrEmpty(department)) {
            return new BaseResponse(-1, "Vui lòng nhập nguồn ứng tuyển");
        }
        return null;
    }

}
