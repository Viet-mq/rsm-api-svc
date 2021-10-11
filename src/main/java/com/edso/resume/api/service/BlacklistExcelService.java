package com.edso.resume.api.service;

import com.edso.resume.lib.entities.HeaderInfo;

import java.io.IOException;

public interface BlacklistExcelService {
    byte[] export(HeaderInfo headerInfo, String name) throws IOException;
}
