package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.service.ReportByDepartmentService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;
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
@RequestMapping("/reportbydepartment")
public class ReportByDepartmentController extends BaseController {
    private final ReportByDepartmentService reportByDepartmentService;

    public ReportByDepartmentController(ReportByDepartmentService reportByDepartmentService) {
        this.reportByDepartmentService = reportByDepartmentService;
    }

    @GetMapping("/list")
    public BaseResponse getReportByDepartmentService(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>getReportByDepartmentService u: {}", headerInfo);
        GetArrayStatisticalReponse<ReportByDepartmentEntity> resp = reportByDepartmentService.findAll(from, to);
        logger.info("<=getReportByDepartmentService u: {}, resp: {}", headerInfo, resp.info());
        return resp;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportReportByDepartment(@RequestHeader Map<String, String> headers,
                                                             @RequestParam(value = "from", required = false) Long from,
                                                             @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportReportByDepartment u: {}", headerInfo);
        ExportResponse response = reportByDepartmentService.exportReportByDepartment(from, to);
        File file = new File(response.getPath());
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "ReportByDepartment.xlsx");
        logger.info("<=exportReportByDepartment u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
