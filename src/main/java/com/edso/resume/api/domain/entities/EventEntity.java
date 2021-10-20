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

    @Override
    public String toString() {
        return "{" +
                "\"type\"=\"" + type + '\"' +
                ", \"profile\"= " + profile +
                '}';
    }
}
