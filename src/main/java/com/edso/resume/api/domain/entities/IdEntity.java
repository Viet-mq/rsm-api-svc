package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdEntity {
    private String historyId;
    private String calendarId;
    private String profileId;
}
