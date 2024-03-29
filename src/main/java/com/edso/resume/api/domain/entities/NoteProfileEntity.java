package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteProfileEntity {
    private String id;
    private String idProfile;
    private String username;
    private String fullName;
    private String comment;
    private String evaluation;
    private String fileName;
    private String url;
    private String updateAt;
    private String updateBy;
}
