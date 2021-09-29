package com.edso.resume.api.domain.request;

import lombok.Data;

@Data
public class CreateHistoryRequest{
    private String idProfile;
    private Long time;
    private String action;
    private String by;

    public CreateHistoryRequest(String idProfile, Long time, String action, String by) {
        this.idProfile = idProfile;
        this.time = time;
        this.action = action;
        this.by = by;
    }
}
