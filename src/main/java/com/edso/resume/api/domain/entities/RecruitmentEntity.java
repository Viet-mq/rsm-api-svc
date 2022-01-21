package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecruitmentEntity {
    private String id;
    private String title;
    private String jobId;
    private String jobName;
    private String addressId;
    private String addressName;
    private String typeOfJob;
    private String quantity;
    private String detailOfSalary;
    private String from;
    private String to;
    private String jobDescription;
    private String requirementOfJob;
    private String interest;
    private Long deadLine;
    private String talentPoolId;
    private String talentPoolName;
    private String salaryDescription;
    private String status;
    private Long createAt;
    private String createBy;
    private List<UserEntity> interviewer;
    private List<RoundEntity> interviewProcess;
}
