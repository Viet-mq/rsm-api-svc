package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceEntity {
    private String sourceCVName;
    private Long count;
}
