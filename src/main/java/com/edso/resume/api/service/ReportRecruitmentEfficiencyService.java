package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;

public interface ReportRecruitmentEfficiencyService {
    GetArrayResponse<ReportRecruitmentEfficiencyEntity> findAll(Long from, Long to);

    ExportResponse exportReportRecruitmentEfficiency(Long from, Long to);
}
