package com.edso.resume.api.domain.request;

import com.edso.resume.lib.entities.HeaderInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class BaseAuthRequest {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9]+[A-Za-z0-9]*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)$";
    private static Pattern pattern;
    private static Matcher matcher;
    @JsonIgnore
    protected HeaderInfo info;

    public BaseAuthRequest() {
        pattern = Pattern.compile(EMAIL_REGEX);
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
