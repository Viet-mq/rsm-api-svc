package com.edso.resume.api.service;

import com.edso.resume.lib.entities.HeaderInfo;

public interface ExcelService {
    String exportExcel(HeaderInfo info);
}
