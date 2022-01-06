package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEntity {
    private String username;
    private String fullName;
    private String avatar;
    private String email;
}
