package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HistoryEmailEntity {
    private String id;
    private String type;
    private String idProfile;
    private String email;
    private String subject;
    private List<FileEntity> files;
    private Long time;
    private String content;
    private String status;
    private String username;
    private String fullName;
}
