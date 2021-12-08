package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reason {
    private String reason;
    private Long count;
    private String percent;

}
