package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReportRecruitmentActivitiesService {
    GetArrayResponse<ReportRecruitmentActivitiesEntity> findAll(Long from, Long to);

    ExportResponse exportReportRecruitmentActivities(Long from, Long to);
}
