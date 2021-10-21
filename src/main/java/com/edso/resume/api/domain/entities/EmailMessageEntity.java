package com.edso.resume.api.domain.entities;

import lombok.Data;

import java.util.Arrays;

@Data
public class EmailMessageEntity {
    private String toEmail;
    private String subject = "THÔNG BÁO SẮP ĐẾN GIỜ PHỎNG VẤN";
    private String message;
    private byte[] file;

//    @Override
//    public String toString() {
//        return
//                "{\"toEmail\":\"" + toEmail + '\"' +
//                        ", \"subject\":\"" + subject + '\"' +
//                        ", \"message\":\"" + message + '\"' +
//                        ", \"file\":\"" + Arrays.toString(file) + '\"' +
//                        '}';
//    }

}
