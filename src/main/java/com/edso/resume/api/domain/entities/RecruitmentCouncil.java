package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class RecruitmentCouncil {
    private String idProfile;
    private String subject;
    private String content;
    private List<String> emails;
    private List<MultipartFile> files;
}
