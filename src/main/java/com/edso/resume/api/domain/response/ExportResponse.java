package com.edso.resume.api.domain.response;

import com.edso.resume.lib.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExportResponse extends BaseResponse {
    private String path;
}
