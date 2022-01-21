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
    public ResponseEntity<Resource> exportListProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "talentPool", required = false) String talentPool,
            @RequestParam(value = "job", required = false) String job,
            @RequestParam(value = "levelJob", required = false) String levelJob,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "recruitment", required = false) String recruitment,
            @RequestParam(value = "calendar", required = false) String calendar,
            @RequestParam(value = "statusCV", required = false) String statusCV) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportListProfile u: {}", headerInfo);
        String pathServer = excelService.exportExcel(headerInfo, fullName, talentPool, job, levelJob, department, recruitment, calendar, statusCV);
        File file = new File(pathServer);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "Profiles.xlsx");
        logger.info("<=exportListProfile u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
