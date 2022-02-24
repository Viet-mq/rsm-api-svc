package com.edso.resume.api.domain.validator;

import lombok.Data;

@Data
public class DictionaryNameValidatorResult {
    private final String type;
    private boolean result;
    private String id;
    private String mail;
    private String key;

    public DictionaryNameValidatorResult(String type) {
        this.type = type;
        this.result = false;
        this.id = "";
        this.mail = "";
    }
}
