package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReportRejectProfileEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.service.ReportRejectProfileService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
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
@RequestMapping("/reportrejectprofile")
public class ReportRejectProfileController extends BaseController {

    private final ReportRejectProfileService reportRejectProfileService;

    public ReportRejectProfileController(ReportRejectProfileService reportRejectProfileService) {
        this.reportRejectProfileService = reportRejectProfileService;
    }

    @GetMapping("/list")
    public BaseResponse getReportRejectProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>getReportRejectProfile u: {}", headerInfo);
        GetArrayResponse<ReportRejectProfileEntity> resp = reportRejectProfileService.findAll(headerInfo, from, to);
        logger.info("<=getReportRejectProfile u: {}, resp: {}", headerInfo, resp.info());
        return resp;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportReportRejectProfile(@RequestHeader Map<String, String> headers,
                                                              @RequestParam(value = "from", required = false) Long from,
                                                              @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportReportRejectProfile u: {}", headerInfo);
        ExportResponse response = reportRejectProfileService.exportReportRejectProfile(headerInfo, from, to);
        File file = new File(response.getPath());
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "ReportRejectProfile.xlsx");
        logger.info("<=exportReportRejectProfile u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
