package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TalentPoolEntity {
    private String id;
    private String name;
    private String description;
    private int numberOfProfile;
    private List<String> managers;
}
