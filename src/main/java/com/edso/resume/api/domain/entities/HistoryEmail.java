package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class HistoryEmail {
    private String id;
    private String idProfile;
    private String subject;
    private List<MultipartFile> files;
    private Long time;
    private String content;
    private String status;
    private String username;
    private String fullName;
}
