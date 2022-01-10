package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportRecruitmentResultEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReportRecruitmentResultService {
    GetArrayResponse<ReportRecruitmentResultEntity> findAll(Long from, Long to);

    ExportResponse exportReportRecruitmentResult(Long from, Long to);
}
