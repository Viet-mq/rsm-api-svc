package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarEntity2 {

    private String id;
    private String idProfile;
    private String fullName;
    private String recruitmentId;
    private String recruitmentName;
    private Long date;
    private Long interviewTime;
    private String interviewAddressId;
    private String interviewAddressName;
    private String floor;
    private String type;
    private List<UserEntity> interviewers;
    private String note;
    private String avatarColor;
    private Long createAt;
    private String createBy;
//    private String sendEmailToInterviewee;
//    private String sendEmailToInterviewer;

}
