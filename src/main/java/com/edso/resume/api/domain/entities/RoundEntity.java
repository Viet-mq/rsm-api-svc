package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoundEntity {
    private String id;
    private String name;
    private Long total;
    private Boolean isDragDisabled;
    private Boolean isNew;
}
