package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlacklistEntity {
    private String id;
    private String email;
    private String phoneNumber;
    private String SSN;
    private String name;
    private String reason;
}
