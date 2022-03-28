package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiEntity {
    private String id;
    private String name;
    private String method;
    private String path;
}
