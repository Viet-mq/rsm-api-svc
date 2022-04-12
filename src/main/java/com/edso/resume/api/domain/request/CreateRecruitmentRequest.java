package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.entities.RoundEntity;
import com.edso.resume.lib.common.ErrorCodeDefs;
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
    private String job;
    private String address;
    private String typeOfJob;
    private Long quantity;
    private String detailOfSalary;
    private Long from;
    private Long to;
    private String jobDescription;
    private String requirementOfJob;
    private String interest;
    private Long deadLine;
    private String talentPool;
    private List<String> interviewer;
    private List<RoundEntity> interviewProcess;
    private String salaryDescription;
    private String department;

    public BaseResponse validate() {
        if (Strings.isNullOrEmpty(title) || title.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.TITLE, "Vui lòng nhập tiêu đề tin tuyển dụng");
        }
        if (Strings.isNullOrEmpty(job) || job.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.LEVEL_JOB, "Vui lòng nhập vị trí tuyển dụng");
        }
        if (Strings.isNullOrEmpty(address) || address.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ADDRESS, "Vui lòng nhập địa điểm làm việc");
        }
        if (!Strings.isNullOrEmpty(typeOfJob) && typeOfJob.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.TYPE_OF_JOB, "Vui lòng nhập loại hình công việc");
        }
        if (quantity == null || quantity <= 0) {
            return new BaseResponse(ErrorCodeDefs.QUANTITY, "Vui lòng nhập số lượng tuyển dụng");
        }
        if (!Strings.isNullOrEmpty(detailOfSalary)) {
            if (!detailOfSalary.equals("Chi tiết mức lương") && !detailOfSalary.equals("Thỏa thuận") && !detailOfSalary.equals("Từ ...") && !detailOfSalary.equals("Up to ...")) {
                return new BaseResponse(ErrorCodeDefs.DETAIL_OF_SALARY, "Vui lòng nhập chi tiết mức lương");
            }
        }
        if (from != null && from < 0) {
            return new BaseResponse(ErrorCodeDefs.FORM, "Vui lòng nhập mức lương từ bao nhiêu");
        }
        if (to != null && to < 0) {
            return new BaseResponse(ErrorCodeDefs.TO, "Vui lòng nhập mức lương đến bao nhiêu");
        }
        if (deadLine == null || deadLine <= 0) {
            return new BaseResponse(ErrorCodeDefs.DEAD_LINE, "Vui lòng nhập hạn nộp hồ sơ");
        }
        if (Strings.isNullOrEmpty(talentPool)) {
            return new BaseResponse(ErrorCodeDefs.TALENT_POOL, "Vui lòng nhập talent pool");
        }
        if (!Strings.isNullOrEmpty(salaryDescription) && salaryDescription.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.SALARY_DESCRIPTION, "Vui lòng nhập mức lương hiển thị ít hơn 255 ký tự");
        }
        if (interviewProcess == null || interviewProcess.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.INTERVIEW_PROCESS, "Vui lòng nhập quy trình tuyển dụng");
        }
        return null;
    }
}
