package com.edso.resume.api.domain.entities;

import lombok.Data;

@Data
public class UserEntity {
    private String username;
    private String fullName;
    private String avatar;
    private String email;
}
