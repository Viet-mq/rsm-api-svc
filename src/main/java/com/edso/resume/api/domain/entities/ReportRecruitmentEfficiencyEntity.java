package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportRecruitmentEfficiencyEntity {
    private String recruitmentName;
    private String createBy;
    private List<StatusEntity> status;
}
