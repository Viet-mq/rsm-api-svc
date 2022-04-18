package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportRecruitmentResultEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReportRecruitmentResultService {
    GetArrayResponse<ReportRecruitmentResultEntity> findAll(HeaderInfo info, Long from, Long to);

    ExportResponse exportReportRecruitmentResult(HeaderInfo info, Long from, Long to);
}
