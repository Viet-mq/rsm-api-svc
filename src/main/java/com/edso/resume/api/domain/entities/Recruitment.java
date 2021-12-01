package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Recruitment {
    private String name;
    private String fullNameCreator;
    private String createBy;
}
