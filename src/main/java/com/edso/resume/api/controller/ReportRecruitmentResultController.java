package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity;
import com.edso.resume.api.domain.entities.ReportRecruitmentResultEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.service.ReportRecruitmentResultService;
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
@RequestMapping("/reportrecruitmentresult")
public class ReportRecruitmentResultController extends BaseController{

    private final ReportRecruitmentResultService reportRecruitmentResultService;

    public ReportRecruitmentResultController(ReportRecruitmentResultService reportRecruitmentResultService) {
        this.reportRecruitmentResultService = reportRecruitmentResultService;
    }

    @GetMapping("/list")
    public BaseResponse getReportRecruitmentResult(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>getReportRecruitmentResult u: {}", headerInfo);
        GetArrayResponse<ReportRecruitmentResultEntity> resp = reportRecruitmentResultService.findAll(from, to);
        logger.info("<=getReportRecruitmentResult u: {}, resp: {}", headerInfo, resp.info());
        return resp;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportReportRecruitmentResult(@RequestHeader Map<String, String> headers,
                                                                      @RequestParam(value = "from", required = false) Long from,
                                                                      @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportReportRecruitmentResult u: {}", headerInfo);
        ExportResponse response = reportRecruitmentResultService.exportReportRecruitmentResult(from, to);
        File file = new File(response.getPath());
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "ReportRecruitmentResult.xlsx");
        logger.info("<=exportReportRecruitmentResult u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
