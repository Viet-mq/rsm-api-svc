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
    //    private List<TalentPool> talentPool;
    private String talentPoolId;
    private String talentPoolName;
    private String username;
    private String hrRef;
    private String mailRef;
    private String mailRef2;
    private String departmentId;
    private String departmentName;
    private String statusCVId;
    private String statusCVName;
    private String image;
    private String cv;
    private String urlCV;
    private String avatarColor;
    private String isNew;
    private List<String> followers;
    private List<String> tags;
    private Long time;
    private String linkedin;
    private String facebook;
    private String skype;
    private String github;
    private String otherTech;
    private String web;
    private String picId;
    private String picName;
    private String picMail;
    private String status;
    private String companyId;
    private String companyName;
    private Long createAt;
}
