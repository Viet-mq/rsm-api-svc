package com.edso.resume.api.domain.entities;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {
    private String type;
    private Object profile;
}
