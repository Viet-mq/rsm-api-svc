package com.edso.resume.api.service;

import com.edso.resume.lib.entities.HeaderInfo;

public interface ExcelService {
    String exportExcel(HeaderInfo info, String fullName, String talentPool, String job, String levelJob, String department, String recruitment, String calendar, String statusCV);
}
