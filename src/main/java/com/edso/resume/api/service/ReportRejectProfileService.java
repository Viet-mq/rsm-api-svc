package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportRejectProfileEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReportRejectProfileService {
    GetArrayResponse<ReportRejectProfileEntity> findAll(HeaderInfo info, Long from, Long to);

    ExportResponse exportReportRejectProfile(HeaderInfo info, Long from, Long to);
}
