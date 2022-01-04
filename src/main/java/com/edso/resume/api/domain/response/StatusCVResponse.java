package com.edso.resume.api.domain.response;

import com.edso.resume.lib.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatusCVResponse extends BaseResponse {
    private String id;
    private String name;
    private Boolean isDragDisabled;
}
