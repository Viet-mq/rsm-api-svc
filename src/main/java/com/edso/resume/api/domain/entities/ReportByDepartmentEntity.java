package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportByDepartmentEntity {
    private String recruitmentName;
    private List<SourceEntity> sources;
}
