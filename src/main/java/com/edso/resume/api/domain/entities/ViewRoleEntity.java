package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ViewRoleEntity {
    private String id;
    private String name;
    private String description;
    private List<Permission> permissions;
}
