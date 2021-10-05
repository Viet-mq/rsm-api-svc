package com.edso.resume.api.controller;

import com.edso.resume.api.service.ExcelService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/excel")
public class ExcelController extends BaseController {

    private ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/export")
    public byte[] exportExcel(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "fullName", required = false) String fullName) throws IOException {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportExcel u: {}, name: {}", headerInfo, fullName);
        byte[] resp = excelService.exportExcel(headerInfo, fullName);
        logger.info("<=exportExcel u: {}, name: {}, resp: {}", headerInfo, fullName, resp);
        return resp;
    }
}
