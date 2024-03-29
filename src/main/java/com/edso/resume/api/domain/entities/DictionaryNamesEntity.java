package com.edso.resume.api.domain.entities;

import lombok.Data;
import org.bson.Document;

import java.util.List;

@Data
public class DictionaryNamesEntity {
    private String schoolName;
    private String jobName;
    private String fullName;
    private String fullNameUser;
    private String fullNamePIC;
    private String levelJobName;
    private String sourceCVName;
    private String talentPoolName;
    private String talentPoolId;
    private String departmentName;
    private String statusCVName;
    private String statusCVId;
    private String idProfile;
    private String recruitmentName;
    private Long recruitmentTime;
    private String addressName;
    private String reason;
    private String recruitmentId;
    private String createRecruitmentBy;
    private String fullNameCreator;
    private String email;
    private String emailUser;
    private String profileEmail;
    private String picEmail;
    private String profilePhoneNumber;
    private String companyName;
    private Document profile;
    private Document document;
    private Document otherProfile;
    private Document statusCV;
    private List<Document> interviewer;
    private List<Document> skill;
}
