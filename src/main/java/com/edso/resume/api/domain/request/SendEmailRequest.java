package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendEmailRequest extends BaseAuthRequest {
    private List<String> profileIds;
    private String subject;
    private String content;
    private List<MultipartFile> files;

    public BaseResponse validate() {
        if (profileIds == null || profileIds.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập profile id");
        }
        if (Strings.isNullOrEmpty(subject) || subject.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập tiêu đề");
        }
        if (Strings.isNullOrEmpty(content)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập nội dung");
        }
        return null;
    }
}
