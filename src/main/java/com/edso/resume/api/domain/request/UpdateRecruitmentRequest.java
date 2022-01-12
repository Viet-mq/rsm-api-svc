package com.edso.resume.api.domain.request;

import com.edso.resume.api.domain.entities.RoundEntity;
import com.edso.resume.lib.common.ErrorCodeDefs;
import com.edso.resume.lib.response.BaseResponse;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateRecruitmentRequest extends BaseAuthRequest {
    private String id;
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
    private String salaryDescription;
    private String status;
    private List<String> interviewer;
    private List<RoundEntity> interviewProcess;

    public BaseResponse validate() {
        if (!Strings.isNullOrEmpty(salaryDescription) && salaryDescription.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.SALARY_DESCRIPTION, "Vui lòng nhập mức lương hiển thị ít hơn 255 ký tự");
        }
        if (Strings.isNullOrEmpty(id) || id.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.ID, "Vui lòng nhập id");
        }
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
            return new BaseResponse(ErrorCodeDefs.FROM, "Vui lòng nhập mức lương từ bao nhiêu");
        }
        if (to != null && to < 0) {
            return new BaseResponse(ErrorCodeDefs.TO, "Vui lòng nhập mức lương đến bao nhiêu");
        }
        if (Strings.isNullOrEmpty(jobDescription) || jobDescription.length() > 100000) {
            return new BaseResponse(ErrorCodeDefs.JOB_DESCRIPTION, "Vui lòng nhập mô tả công việc chi tiết");
        }
        if (Strings.isNullOrEmpty(requirementOfJob) || requirementOfJob.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.REQUIREMENT_OF_JOB, "Vui lòng nhập yêu cầu công việc");
        }
        if (!Strings.isNullOrEmpty(interest) && interest.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.INTEREST, "Vui lòng nhập quyền lợi");
        }
        if (deadLine == null || deadLine <= 0) {
            return new BaseResponse(ErrorCodeDefs.DEAD_LINE, "Vui lòng nhập hạn nộp hồ sơ");
        }
        if (!Strings.isNullOrEmpty(talentPool) && talentPool.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.TALENT_POOL, "Vui lòng nhập talent pool");
        }
        if (!Strings.isNullOrEmpty(status) && status.length() > 255) {
            return new BaseResponse(ErrorCodeDefs.STATUS, "Vui lòng nhập trạng thái tin tuyển dụng");
        }
        if (interviewProcess == null || interviewProcess.isEmpty()) {
            return new BaseResponse(ErrorCodeDefs.INTERVIEW_PROCESS, "Vui lòng nhập quy trình tuyển dụng");
        }
        return null;
    }
}
