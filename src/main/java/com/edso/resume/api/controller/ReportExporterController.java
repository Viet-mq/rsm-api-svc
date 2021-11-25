package com.edso.resume.api.controller;

import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.service.ReportByDepartmentService;
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
@RequestMapping("/report")
public class ReportExporterController extends BaseController{

    private final ReportByDepartmentService reportByDepartmentService;

    public ReportExporterController(ReportByDepartmentService reportByDepartmentService) {
        this.reportByDepartmentService = reportByDepartmentService;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportReportByDepartment(@RequestHeader Map<String, String> headers, @RequestParam Long from, @RequestParam Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportReportByDepartment u: {}", headerInfo);
        ExportResponse response = reportByDepartmentService.exportReportByDepartment(from, to);
        String pathServer = response.getPath();
        File file = new File(pathServer);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        }catch (Throwable e){
            logger.error("Exception: ", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "Report.xlsx");
        logger.info("<=exportReportByDepartment u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
