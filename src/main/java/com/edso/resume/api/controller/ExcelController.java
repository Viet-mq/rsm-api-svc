package com.edso.resume.api.controller;

import com.edso.resume.api.service.ExcelService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/excel")
public class ExcelController extends BaseController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportExcel(@RequestHeader Map<String, String> headers) throws IOException {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportExcel u: {}", headerInfo);
        String pathServer = excelService.exportExcel(headerInfo);
        File file = new File(pathServer);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "Profiles.xlsx");
        logger.info("<=exportExcel u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
