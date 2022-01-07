package com.edso.resume.api.domain.validator;

import com.edso.resume.api.domain.entities.TalentPoolEntity;
import lombok.Data;

@Data
public class TalentPoolResult {
    private final String key;
    private boolean result;
    private TalentPoolEntity talentPool;

    public TalentPoolResult(String key) {
        this.key = key;
        this.result = false;
    }
}
