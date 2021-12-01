package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportRecruitmentResultEntity {
    private String recruitmentName;
    private Integer needToRecruit;
    private Integer recruited;
    private String percent;
}
