package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecruitmentEntity {
    private String id;
    private String title;
    private String levelJobId;
    private String levelJobName;
    private String address;
    private String typeOfJob;
    private String quantity;
    private String detailOfSalary;
    private String jobDescription;
    private String requirementOfJob;
    private Long deadLine;
    private String talentPoolId;
    private String talentPoolName;
    private List<UserEntity> interviewer;
}
