package com.edso.resume.api.domain.request;

import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateRecruitmentRequest extends BaseAuthRequest {
    private String title;
    private String levelJob;
    private String address;
    private String typeOfJob;
    private Long quantity;
    private String detailOfSalary;
    private Long from;
    private Long to;
    private String jobDescription;
    private String requirementOfJob;
    private Long deadLine;
    private String talentPool;
    private List<String> interviewer;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(title) || title.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập tiêu đề tin tuyển dụng");
        }
        if (Strings.isNullOrEmpty(levelJob) || levelJob.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập vị trí tuyển dụng");
        }
        if (Strings.isNullOrEmpty(address) || address.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập địa điểm làm việc");
        }
        if (!Strings.isNullOrEmpty(typeOfJob) && typeOfJob.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập loại hình công việc");
        }
        if (quantity == null || quantity > 0) {
            return new BaseResponse(-1, "Vui lòng nhập số lượng tuyển dụng");
        }
        if (!Strings.isNullOrEmpty(detailOfSalary)) {
            if(!detailOfSalary.equals("Chi tiết mức lương") && !detailOfSalary.equals("Thỏa thuận") && !detailOfSalary.equals("Từ ...") && !detailOfSalary.equals("Up to ...")){
                return new BaseResponse(-1, "Vui lòng nhập chi tiết mức lương");
            }
        }
        if(from != null && from <= 0){
            return new BaseResponse(-1, "Vui lòng nhập mức lương từ bao nhiêu");
        }
        if(to != null && to <= 0){
            return new BaseResponse(-1, "Vui lòng nhập mức lương đến bao nhiêu");
        }
        if (Strings.isNullOrEmpty(jobDescription) || jobDescription.length() > 1000) {
            return new BaseResponse(-1, "Vui lòng nhập mô tả công việc chi tiết");
        }
        if (Strings.isNullOrEmpty(requirementOfJob) || requirementOfJob.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập yêu cầu công việc");
        }
        if (deadLine == null || deadLine < 0) {
            return new BaseResponse(-1, "Vui lòng nhập hạn nộp hồ sơ");
        }
        if (Strings.isNullOrEmpty(talentPool) || talentPool.length() > 255) {
            return new BaseResponse(-1, "Vui lòng nhập talent pool");
        }
        return null;
    }
}
