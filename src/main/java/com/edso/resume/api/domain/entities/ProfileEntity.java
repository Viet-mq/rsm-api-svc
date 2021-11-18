package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileEntity {
    private String id;
    private String fullName;
    private Long dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private String hometown;
    private String levelSchool;
    private String schoolId;
    private String schoolName;
    private Long dateOfApply;
    private String sourceCVId;
    private String sourceCVName;
    private String jobId;
    private String jobName;
    private List<SkillEntity> skill;
    private String levelJobId;
    private String levelJobName;
    private String recruitmentId;
    private String recruitmentName;
    private String talentPoolId;
    private String talentPoolName;
    private String hrRef;
    private String mailRef;
    private String departmentId;
    private String departmentName;
    private String statusCVId;
    private String statusCVName;
    private String image;
    private String cv;
    private String urlCV;
    private String avatarColor;
    private String isNew;
}
