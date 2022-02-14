package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReminderEntity {
    private String id;
    private String title;
    private Long start;
    private Long end;
    private String desc;
    private String createAt;
    private String createBy;
    private String updateAt;
    private String updateBy;
}
