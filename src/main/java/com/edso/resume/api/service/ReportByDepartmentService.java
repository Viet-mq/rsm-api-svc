package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.entities.ReportByDepartmentEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;

public interface ReportByDepartmentService {
    GetArrayStatisticalReponse<ReportByDepartmentEntity2> findAll(Long from, Long to);

    ExportResponse exportReportByDepartment(Long from, Long to);
}
