package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeEntity {
    private String id;
    private Long time;
    private String check;
    private int nLoop;
    private String email;
}
