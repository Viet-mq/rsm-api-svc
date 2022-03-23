package com.edso.resume.api.domain.entities;

import lombok.Data;

import java.util.List;

@Data
public class ProfileRabbitMQEntity {
    private String id;
    private String fullName;
    private String phoneNumber;
    private String gender;
    private String email;
    private Long dateOfBirth;
    private String hometown;
    private String schoolId;
    private String schoolName;
    private String jobId;
    private String jobName;
    private String levelJobId;
    private String levelJobName;
    private String sourceCVId;
    private String sourceCVName;
    private String hrRef;
    private String mailRef;
    private String mailRef2;
    private List<String> skill;
    private Long dateOfApply;
    private String statusCVId;
    private String statusCVName;
    private String talentPoolId;
    private String talentPoolName;
    private String schoolLevel;
    private String evaluation;
    private Long lastApply;
    private String departmentId;
    private String departmentName;
    private String levelSchool;
    private String recruitmentId;
    private String recruitmentName;
    private String avatarColor;
    private Boolean isNew;
    private Long time;
    private String linkedin;
    private String facebook;
    private String skype;
    private String github;
    private String otherTech;
    private String web;
    private String picId;
    private String picName;
    private String status;
    private String companyId;
    private String companyName;
    private String username;
    private List<String> followers;
    private List<String> tags;
    private String picMail;
}
