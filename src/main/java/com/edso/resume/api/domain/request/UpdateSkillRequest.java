package com.edso.resume.api.domain.request;

import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.common.NameConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UpdateSkillRequest extends BaseAuthRequest{
    private String id;
    private String name;
    private List<String> jobs;
    private String status;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
        if (Strings.isNullOrEmpty(name) || name.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.NAME, "Vui lòng nhập tên kỹ năng công việc");
        }
//        if (Strings.isNullOrEmpty(status) || status.length() > 255) {
//            return new BaseResponse(ErrorCodeDefs.STATUS, "Vui lòng nhập trạng thái");
//        }
//        if (!status.equals(NameConfig.DANG_SU_DUNG) && !status.equals(NameConfig.NGUNG_SU_DUNG)) {
//            return new BaseResponse(ErrorCodeDefs.STATUS, "Vui lòng nhập trạng thái: Đang sử dụng hoặc Ngừng sử dụng");
//        }
        return null;
    }
}
