package com.edso.resume.api.domain.entities;

import lombok.Builder;

@Builder
public class FileEntity {
    private String fileName;
    private String filePath;
}
