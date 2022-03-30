package com.edso.resume.api.domain.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CandidateRequest {
    private String emailCandidate;
    private String subjectCandidate;
    private String contentCandidate;
    private List<MultipartFile> fileCandidates;
}
