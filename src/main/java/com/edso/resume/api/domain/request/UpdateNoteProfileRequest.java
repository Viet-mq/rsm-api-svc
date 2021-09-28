package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UpdateNoteProfileRequest extends BaseAuthRequest{
    private String id;
    private String idProfile;
    private String note;

    public BaseResponse validate(){
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(-1, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(idProfile)) {
            return new BaseResponse(-1, "Vui lòng nhập id profile");
        }
        if (Strings.isNullOrEmpty(note)) {
            return new BaseResponse(-1, "Vui lòng nhập lưu ý");
        }
        return null;
    }
}
