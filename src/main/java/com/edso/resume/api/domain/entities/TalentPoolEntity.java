package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TalentPoolEntity implements Comparable<TalentPoolEntity> {
    private String id;
    private String name;
    private String description;
    private Long numberOfProfile;
    private long createAt;
    private String createBy;
    private long total;
    private List<String> managers;

    @Override
    public int compareTo(TalentPoolEntity o) {
        return -this.getName().compareTo(o.getName());
    }
}
