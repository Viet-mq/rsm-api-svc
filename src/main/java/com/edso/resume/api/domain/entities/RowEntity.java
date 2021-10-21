package com.edso.resume.api.domain.entities;

import org.springframework.web.multipart.MultipartFile;

public class RowEntity {
    private String username;
    private String fullName;
    private String comment;
    private String evaluation;
    private MultipartFile file;
}
