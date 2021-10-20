package com.edso.resume.api.domain.request;

import com.edso.resume.lib.entities.HeaderInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class BaseAuthRequest {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9]+[A-Za-z0-9]*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)$";
    private static final String PHONE_NUMBER_REGEX = "(^$|[0-9]{10})";
    private static final String SSN_REGEX = "[0-9]{12}";
    private static Pattern pattern;
    private static Pattern phoneNumPattern;
    private static Pattern SsnPattern;
    private static Matcher matcher;
    @JsonIgnore
    protected HeaderInfo info;

    public BaseAuthRequest() {
        pattern = Pattern.compile(EMAIL_REGEX);
        phoneNumPattern = Pattern.compile(PHONE_NUMBER_REGEX);
        SsnPattern = Pattern.compile(SSN_REGEX);
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean validatePhoneNumber(String phoneNumber) {
        matcher = phoneNumPattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public boolean validateSSN(String SSN) {
        matcher = SsnPattern.matcher(SSN);
        return matcher.matches();
    }

}
