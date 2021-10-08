package com.edso.resume.api.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateHistoryRequest {
    private String idProfile;
    private Long time;
    private String action;
    private String by;
}
