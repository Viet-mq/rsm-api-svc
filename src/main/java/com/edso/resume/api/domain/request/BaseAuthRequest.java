package com.edso.resume.api.domain.request;

import com.edso.resume.lib.entities.HeaderInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class BaseAuthRequest {

    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
    private static final String PHONE_NUMBER_REGEX = "(^$|[0-9]{10})";
    private static final String SSN_REGEX = "[0-9]{12}";
    private static final String FULL_NAME_REGEX = "^[\\p{L} .'-]+$";
    private static Pattern pattern;
    private static Pattern phoneNumPattern;
    private static Pattern SsnPattern;
    private static Pattern fullNamePattern;
    private static Matcher matcher;
    @JsonIgnore
    protected HeaderInfo info;

    public BaseAuthRequest() {
        pattern = Pattern.compile(EMAIL_REGEX);
        phoneNumPattern = Pattern.compile(PHONE_NUMBER_REGEX);
        SsnPattern = Pattern.compile(SSN_REGEX);
        fullNamePattern = Pattern.compile(FULL_NAME_REGEX);
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean validateSmt() {
        return true;
    }

    public boolean validatePhoneNumber(String phoneNumber) {
        matcher = phoneNumPattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public boolean validateSSN(String SSN) {
        matcher = SsnPattern.matcher(SSN);
        return matcher.matches();
    }

    public boolean validateFullName(String fullName) {
        matcher = fullNamePattern.matcher(fullName);
        return matcher.matches();
    }

}
