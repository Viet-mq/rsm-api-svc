package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class HistoryEmails {
    private List<IdEntity> ids;
    private String subject;
    private List<MultipartFile> files;
    private String content;
}
