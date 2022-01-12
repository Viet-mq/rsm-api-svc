package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class Candidate {
    private String id;
    private String idProfile;
    private String subject;
    private String content;
    private String email;
    private List<MultipartFile> files;
}
