package com.edso.resume.api.domain.entities;

import lombok.*;

@Data
@AllArgsConstructor
public class EventEntity {
    private String type;
    private Object profile;
}
