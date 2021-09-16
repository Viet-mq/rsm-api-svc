package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentEntity {
    private String id;
    private String name;
}
