package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatusCVEntity {
    private String id;
    private String name;
    private List<ChildrenStatusCVEntity> children;
    private Integer round;
    private Boolean delete;

}
