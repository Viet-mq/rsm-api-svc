package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteProfileEntity {
    private String id;
    private String idProfile;
    private String note;
    private String create_at;
    private String create_by;
}
