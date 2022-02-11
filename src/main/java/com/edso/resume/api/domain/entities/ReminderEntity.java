package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReminderEntity {
    private String id;
    private String content;
    private Long time;
    private String repeat;
    private String createAt;
    private String createBy;
    private String updateAt;
    private String updateBy;
}
