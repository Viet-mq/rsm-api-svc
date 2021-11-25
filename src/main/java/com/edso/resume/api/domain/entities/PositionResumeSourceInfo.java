package com.edso.resume.api.domain.entities;

import lombok.Data;

import java.util.Map;

@Data
public class PositionResumeSourceInfo {
    private String name;
    private Map<String, Integer> data;
}
