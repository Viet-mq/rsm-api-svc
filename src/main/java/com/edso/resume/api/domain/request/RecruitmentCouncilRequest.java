package com.edso.resume.api.domain.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class RecruitmentCouncilRequest {
    private String emailRecruitmentCouncil;
    private String subjectRecruitmentCouncil;
    private String contentRecruitmentCouncil;
    private List<MultipartFile> fileRecruitmentCouncils;
}
