package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.swing.text.Document;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateOrganizationRequest extends BaseAuthRequest{
    private String name;
    private String description;
    private List<Document> organizations;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(name)) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên tổ chức");
        }
        if (Strings.isNullOrEmpty(description)) {
            return new BaseResponse(ErrorCodeDefs.DESCRIPTION, "Vui lòng nhập mô tả");
        }
        if (organizations == null || organizations.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.ORGANIZATIONS, "Vui lòng nhập các tổ chức");
        }
        return null;
    }
}
