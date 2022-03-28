package com.edso.resume.api.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.bson.Document;

import java.util.List;

@Data
@Builder
public class RoleEntity {
    private String id;
    private String name;
    private String description;
    @JsonProperty("view_roles")
    private List<Document> viewRoles;
    @JsonProperty("api_roles")
    private List<Document> apiRoles;
}
