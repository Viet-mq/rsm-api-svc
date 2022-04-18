package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;

public interface ReportByDepartmentService {
    GetArrayStatisticalReponse<ReportByDepartmentEntity2> findAll(HeaderInfo info, Long from, Long to);

    ExportResponse exportReportByDepartment(HeaderInfo info, Long from, Long to);
}
