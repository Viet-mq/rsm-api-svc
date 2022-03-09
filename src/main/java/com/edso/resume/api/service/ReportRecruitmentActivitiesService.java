package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReportRecruitmentActivitiesService {
    GetArrayResponse<ReportRecruitmentActivitiesEntity2> findAll(Long from, Long to);

    ExportResponse exportReportRecruitmentActivities(Long from, Long to);
}
