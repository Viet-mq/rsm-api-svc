package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateDetailProfileRequest extends BaseAuthRequest {

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
    private String cv;
    private String sourceCV;
    private String hrRef;
    private Long dateOfApply;
    private String cvType;
    private Long lastApply;
    private String tags;
    private String note;
    private String evaluation;
    private String talentPool;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(gender)) {
            return new BaseResponse(-1, "Vui lòng nhập giới tính");
        }
        if (!gender.equals("Nam") && !gender.equals("Nữ")) {
            return new BaseResponse(-1, "Vui lòng nhập Nam hoặc Nữ");
        }
        if (Strings.isNullOrEmpty(tags)) {
            return new BaseResponse(-1, "Vui lòng nhập tags");
        }
        if (Strings.isNullOrEmpty(note)) {
            return new BaseResponse(-1, "Vui lòng nhập note");
        }
        if (lastApply <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập thời gian ứng tuyển gần nhất");
        }
        if (Strings.isNullOrEmpty(evaluation)) {
            return new BaseResponse(-1, "Vui lòng nhập đánh giá");
        }
        if (Strings.isNullOrEmpty(fullName)) {
            return new BaseResponse(-1, "Vui lòng nhập họ và tên");
        }
        if (dateOfBirth <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập ngày tháng năm sinh");
        }
        if (Strings.isNullOrEmpty(hometown)) {
            return new BaseResponse(-1, "Vui lòng nhập quê quán");
        }
        if (Strings.isNullOrEmpty(school)) {
            return new BaseResponse(-1, "Vui lòng nhập trường học");
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
        if (Strings.isNullOrEmpty(job)) {
            return new BaseResponse(-1, "Vui lòng nhập tên công việc");
        }
        if (Strings.isNullOrEmpty(levelJob)) {
            return new BaseResponse(-1, "Vui lòng nhập vị trí tuyển dụng");
        }
        if (Strings.isNullOrEmpty(cv)) {
            return new BaseResponse(-1, "Vui lòng nhập cv");
        }
        if (Strings.isNullOrEmpty(sourceCV)) {
            return new BaseResponse(-1, "Vui lòng nhập nguồn cv");
        }
        if (Strings.isNullOrEmpty(hrRef)) {
            return new BaseResponse(-1, "Vui lòng nhập HR ref");
        }
        if (dateOfApply <= 0) {
            return new BaseResponse(-1, "Vui lòng nhập ngày apply");
        }
        if (Strings.isNullOrEmpty(cvType)) {
            return new BaseResponse(-1, "Vui lòng nhập kiểu cv");
        }
        if (Strings.isNullOrEmpty(talentPool)) {
            return new BaseResponse(-1, "Vui lòng nhập kiểu cv");
        }
        return null;
    }

}
