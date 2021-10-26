package com.edso.resume.api.domain.validator;

import lombok.Data;

@Data
public class DictionaryValidatorResult {

    private final String type;
    private boolean result;
    private String name;
    private String idProfile;
    private String key;

    public DictionaryValidatorResult(String type) {
        this.type = type;
        this.result = false;
        this.name = "";
        this.idProfile = "";
    }

}
