package com.edso.resume.api.domain.response;

import com.edso.resume.lib.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRecruitmentResponse<T> extends BaseResponse {
    private T recruitment;

    public GetRecruitmentResponse() {
        super();
    }

    public String info() {
        return "rc = " + code + ", rd = " + message + ", recruitment = " + recruitment;
    }

    public void setSuccess(T recruitment) {
        super.setSuccess();
        this.recruitment = recruitment;
    }
}
