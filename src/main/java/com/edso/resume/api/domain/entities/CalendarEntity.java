package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class CalendarEntity {
    private String id;
    private String idProfile;
    private String time;
    private String address;
    private String form;
    private List<String> interviewer;
    private String interviewee;
    private String content;
    private List<String> question;
    private List<String> comment;
    private String evaluation;
    private String status;
    private String reason;
    private String timeStart;
    private String timeFinish;
}
