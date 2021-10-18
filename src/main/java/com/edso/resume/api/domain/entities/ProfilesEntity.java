package com.edso.resume.api.domain.entities;

import lombok.Data;

@Data
public class ProfilesEntity {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String hometown;
    private String schoolLevel;
    private String schoolName;
    private String major;
    private String recentWorkPlace;
    private String dateOfApply;
    private String sourceCVName;
}
