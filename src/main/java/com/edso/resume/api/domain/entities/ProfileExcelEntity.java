package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileExcelEntity {
    private String id;
    private String fullName;
    private String gender;
    private String phoneNumber;
    private String email;
    private String dateOfBirth;
    private String hometown;
    private String schoolName;
    private String jobName;
    private String levelJobName;
    private String cv;
    private String sourceCVName;
    private String hrRef;
    private String dateOfApply;
    private String cvType;
    private String lastApply;
    private String tags;
    private String dateOfCreate;
    private String dateOfUpdate;
    private String note;
    private String evaluation;
    private String statusCVName;
}
