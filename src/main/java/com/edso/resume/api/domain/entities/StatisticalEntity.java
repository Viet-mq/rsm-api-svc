package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticalEntity {
    private String title;
    private List<SourceEntity> Sources;
}
