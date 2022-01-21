package com.edso.resume.api.domain.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> List<T> convertJsonToList(String json, Class<T[]> className) {
        if (Strings.isNullOrEmpty(json)) {
            return new ArrayList<>();
        }
        try {
            T[] pp = mapper.readValue(json, className);
            return Arrays.asList(pp);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
