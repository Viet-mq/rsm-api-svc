package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryEntity {
    private String id;
    private String idProfile;
    private String type;
    private Long time;
    private String action;
    private String username;
    private String fullName;
}
