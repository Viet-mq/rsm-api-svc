package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkillEntity {
    private String id;
    private String name;
    private List<CategoryEntity> jobs;
    private String status;
}
