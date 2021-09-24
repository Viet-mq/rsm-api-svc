package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryEntity {
    private String id;
    private String idProfile;
    private String time;
    private String action;
    private String by;
}
