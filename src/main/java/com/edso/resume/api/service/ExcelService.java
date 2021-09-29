package com.edso.resume.api.service;

import com.edso.resume.lib.entities.HeaderInfo;

import java.io.IOException;

public interface ExcelService {
    byte[] exportExcel(HeaderInfo info, String fullName) throws IOException;
}
