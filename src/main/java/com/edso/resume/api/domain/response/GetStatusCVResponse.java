package com.edso.resume.api.domain.response;

import com.edso.resume.lib.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class GetStatusCVResponse<T> extends BaseResponse {
    private long total;
    private Set<T> rows;

    public GetStatusCVResponse() {
        super();
        this.total = 0;
        this.rows = new HashSet<>();
    }

    public String info() {
        return "rc = " + code + ", rd = " + message + ", size = " + (rows != null ? rows.size() : 0) + ", total = " + total;
    }

    public void setSuccess(long total, Set<T> rows) {
        super.setSuccess();
        this.total = total;
        this.rows = rows;
    }
}
