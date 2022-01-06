package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TalentPool {
    private String id;
    private Long time;
}
