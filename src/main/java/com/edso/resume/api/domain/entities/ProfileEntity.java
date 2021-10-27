package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileEntity {
    private String id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Long dateOfBirth;
    private String hometown;
    private String schoolId;
    private String schoolName;
    private String jobId;
    private String jobName;
    private String levelJobId;
    private String levelJobName;
    private String cv;
    private String sourceCVId;
    private String sourceCVName;
    private String hrRef;
    private Long dateOfApply;
    private String cvType;
    private String statusCVId;
    private String statusCVName;
    private String talentPoolId;
    private String talentPoolName;
}
