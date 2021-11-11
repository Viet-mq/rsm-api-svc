package com.edso.resume.api.domain.entities;

import lombok.Data;

import org.bson.Document;
import java.util.List;

@Data
public class DictionaryNamesEntity {
    private String schoolName;
    private String jobName;
    private String levelJobName;
    private String sourceCVName;
    private String talentPoolName;
    private String departmentName;
    private String statusCVName;
    private String idProfile;
    private String email;
    private List<Document> interviewer;
}
