package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarEntity {
    private String id;
    private String idProfile;
    private Long time;
    private String address;
    private String form;
    private List<String> interviewer;
    private String interviewee;
    private String content;
    private String question;
    private String comments;
    private String evaluation;
    private String statusId;
    private String statusName;
    private String reason;
    private Long timeStart;
    private Long timeFinish;
}
