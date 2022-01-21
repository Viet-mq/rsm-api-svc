package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Email {
    private String type;
    private String profileId;
    private String historyId;
    private String subject;
    private String content;
    private List<String> files;
}
