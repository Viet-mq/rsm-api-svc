package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReportRecruitmentActivitiesEntity2 {
    private String fullName;
    private String createBy;
    private Long recruitmentTotal;
    private Long noteTotal;
    private Map<String, Long> status;
}
