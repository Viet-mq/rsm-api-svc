package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import javax.swing.text.Document;
import java.util.List;

@Data
@Builder
public class OrganizationEntity {
    private String id;
    private String name;
    private String description;
    private List<Document> organizations;
}
