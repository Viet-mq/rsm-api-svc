package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceCVEntity {
    private String id;
    private String name;
}
