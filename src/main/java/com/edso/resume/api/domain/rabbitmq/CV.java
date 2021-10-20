package com.edso.resume.api.domain.rabbitmq;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CV {
    private String id;
    private String type;
    private String url;
    private String fileName;

    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id + '\"' +
                ", \"type\":\"" + type + '\"' +
                ", \"url\":\"" + url + '\"' +
                ", \"fileName\":\"" + fileName + '\"' +
                '}';
    }
}