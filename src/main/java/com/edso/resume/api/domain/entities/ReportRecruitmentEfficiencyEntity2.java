package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReportRecruitmentEfficiencyEntity2 {
    private String recruitmentName;
    private String createBy;
    private Map<String, Long> status;
}
