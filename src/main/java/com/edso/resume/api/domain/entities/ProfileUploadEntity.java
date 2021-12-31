package com.edso.resume.api.domain.entities;

import lombok.Data;

@Data
public class ProfileUploadEntity {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String hometown;
    private String levelSchool;
    private String schoolName;
    private String jobName;
    private String dateOfApply;
    private String sourceCVName;
    private String talentPoolName;
    private String hrRef;
    private String departmentName;
}
