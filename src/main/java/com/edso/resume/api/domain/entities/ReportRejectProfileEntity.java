package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportRejectProfileEntity {
    private String sheet;
    private List<Reason> reasons;
    private Long total;
}
