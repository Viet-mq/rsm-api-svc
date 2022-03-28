package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;
import org.bson.Document;

import java.util.List;

@Data
@Builder
public class PermissionEntity {
    private String id;
    private String title;
    private String path;
    private String icon;
    private Long index;
    private List<Document> actions;
}
