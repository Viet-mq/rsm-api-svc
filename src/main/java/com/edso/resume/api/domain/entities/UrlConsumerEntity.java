package com.edso.resume.api.domain.entities;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UrlConsumerEntity {
    private String id;
    private String type;
    private String url;
    private String fileName;
}
