package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;
import org.bson.Document;

import java.util.List;

@Data
@Builder
public class OrganizationEntity {
    private String id;
    private String name;
    private String description;
    private List<String> organizations;
}
