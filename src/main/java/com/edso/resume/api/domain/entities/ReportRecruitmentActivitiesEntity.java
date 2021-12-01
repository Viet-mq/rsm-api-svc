package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportRecruitmentActivitiesEntity {
    private String fullName;
    private String createBy;
    private Long recruitmentTotal;
    private Long noteTotal;
    private List<StatusEntity> status;
}
