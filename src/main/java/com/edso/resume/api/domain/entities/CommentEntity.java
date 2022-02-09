package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentEntity {
    private String id;
    private String idProfile;
    private String content;
    private String createAt;
    private String createBy;
    private String updateAt;
    private String updateBy;
}
