package com.edso.resume.api.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventImageEntity {
    private String type;
    private ImageEntity image;
}
