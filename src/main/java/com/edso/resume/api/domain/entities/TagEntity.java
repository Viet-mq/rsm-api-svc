package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagEntity {
    private String id;
    private String name;
    private String color;
}
