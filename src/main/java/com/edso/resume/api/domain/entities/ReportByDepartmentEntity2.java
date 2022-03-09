package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReportByDepartmentEntity2 {
    private String recruitmentName;
    private Map<String, Long> sources;
}
