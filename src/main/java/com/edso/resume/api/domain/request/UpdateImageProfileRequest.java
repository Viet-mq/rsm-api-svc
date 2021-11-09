package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
public class UpdateImageProfileRequest extends BaseAuthRequest {

    private String idProfile;
    private MultipartFile image;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(idProfile) || idProfile.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        return null;
    }
}
