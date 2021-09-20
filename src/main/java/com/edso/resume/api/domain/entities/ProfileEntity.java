package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileEntity {
    private String id;
    private String fullName;
    private String dateOfBirth;
    private String hometown;
    private String school;
    private String phonenumber;
    private String email;
    private String job;
    private String levelJob;
    private String cv;
    private String sourceCV;
    private String hrRef;
    private String dateOfApply;
    private String cvType;

}
