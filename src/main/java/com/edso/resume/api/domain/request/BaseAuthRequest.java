package com.edso.resume.api.domain.request;

import com.edso.resume.lib.entities.HeaderInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class BaseAuthRequest {

    private static final String SSN_REGEX = "[0-9]{12}";
    private static Pattern SsnPattern;
    private static Pattern fullNamePattern;
    private static Matcher matcher;
    @JsonIgnore

    protected HeaderInfo info;

    public BaseAuthRequest() {
        SsnPattern = Pattern.compile(SSN_REGEX);
    }

    public boolean validateSmt() {
        return true;
    }

    public boolean validateSSN(String SSN) {
        matcher = SsnPattern.matcher(SSN);
        return matcher.matches();
    }

}
