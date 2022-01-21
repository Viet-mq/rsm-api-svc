package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Emails {
    private String type;
    private List<IdEntity> ids;
    private String subject;
    private String content;
    private List<String> files;
}
