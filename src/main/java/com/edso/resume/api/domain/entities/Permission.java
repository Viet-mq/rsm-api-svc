package com.edso.resume.api.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Permission {
    @JsonProperty("permission_id")
    private String permissionId;
    private List<String> actions;
}
