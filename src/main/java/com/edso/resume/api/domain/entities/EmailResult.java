package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailResult {
    private String email;
    private String subject;
    private String content;

}
