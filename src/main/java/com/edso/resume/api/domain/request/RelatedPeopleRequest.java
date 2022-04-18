package com.edso.resume.api.domain.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class RelatedPeopleRequest {
    private List<String> usernameRelatedPeoples;
    private String subjectRelatedPeople;
    private String contentRelatedPeople;
    private List<MultipartFile> fileRelatedPeoples;
}
