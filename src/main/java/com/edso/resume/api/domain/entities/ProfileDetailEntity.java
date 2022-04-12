package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileDetailEntity {
    private String id;
    private String fullName;
    private String gender;
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
    private String sourceCVId;
    private String sourceCVName;
    private String username;
    private String hrRef;
    private String mailRef;
    private String mailRef2;
    private Long dateOfApply;
    private List<SkillEntity> skill;
    private Long lastApply;
    private Long dateOfCreate;
    private Long dateOfUpdate;
    private String evaluation;
    private String statusCVId;
    private String statusCVName;
    //    private List<TalentPool> talentPool;
    private String talentPoolId;
    private String talentPoolName;
    private String image;
    private String urlCV;
    private String departmentId;
    private String departmentName;
    private String levelSchool;
    private String recruitmentId;
    private String recruitmentName;
    private String avatarColor;
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
