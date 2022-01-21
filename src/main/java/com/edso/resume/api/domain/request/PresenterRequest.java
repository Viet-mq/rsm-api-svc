package com.edso.resume.api.domain.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PresenterRequest {
    private String subjectPresenter;
    private String contentPresenter;
    private List<MultipartFile> filePresenters;
}
