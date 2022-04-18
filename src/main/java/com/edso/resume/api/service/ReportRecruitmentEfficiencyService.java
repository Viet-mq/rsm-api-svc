package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReportRecruitmentEfficiencyService {
    GetArrayResponse<ReportRecruitmentEfficiencyEntity2> findAll(HeaderInfo info, Long from, Long to);

    ExportResponse exportReportRecruitmentEfficiency(HeaderInfo info, Long from, Long to);
}
