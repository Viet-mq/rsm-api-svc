package com.edso.resume.api.domain.validator;

import lombok.Data;
import org.bson.Document;


@Data
public class DictionaryValidatorResult {

    private final String type;
    private boolean result;
    private Object name;
    private String idProfile;
    private String fullName;
    private String statusCVId;
    private String mailRef;
    private Document document;
    private String key;

    public DictionaryValidatorResult(String type) {
        this.type = type;
        this.result = false;
        this.name = "";
        this.idProfile = "";
        this.fullName = "";
        this.statusCVId = "";
        this.mailRef = "";
        this.document = new Document();
    }

}
