package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UpdatePermissionRequest extends BaseAuthRequest {

    private String id;
    private String title;
    private String icon;
    private String path;
    private Long index;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id)) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (index == null || index <= 0) {
            return new BaseResponse(ErrorCodeDefs.INDEX, "Vui lòng nhập số thứ tự");
        }
        if (Strings.isNullOrEmpty(title)) {
            return new BaseResponse(ErrorCodeDefs.TITLE, "Vui lòng nhập title");
        }
        if (Strings.isNullOrEmpty(icon)) {
            return new BaseResponse(ErrorCodeDefs.ICON, "Vui lòng nhập icon");
        }
        if (Strings.isNullOrEmpty(path)) {
            return new BaseResponse(ErrorCodeDefs.PATH, "Vui lòng nhập path");
        }
        return null;
    }
}
