package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.service.ReportRecruitmentActivitiesService;
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
@RequestMapping("/reportrecruitmentactivities")
public class ReportRecruitmentActivitiesController extends BaseController {

    private final ReportRecruitmentActivitiesService reportRecruitmentActivitiesService;

    public ReportRecruitmentActivitiesController(ReportRecruitmentActivitiesService reportRecruitmentActivitiesService) {
        this.reportRecruitmentActivitiesService = reportRecruitmentActivitiesService;
    }

    @GetMapping("/list")
    public BaseResponse getReportRecruitmentActivities(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>getReportRecruitmentActivities u: {}", headerInfo);
        GetArrayResponse<ReportRecruitmentActivitiesEntity2> resp = reportRecruitmentActivitiesService.findAll(from, to);
        logger.info("<=getReportRecruitmentActivities u: {}, resp: {}", headerInfo, resp.info());
        return resp;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportReportRecruitmentActivities(@RequestHeader Map<String, String> headers,
                                                                      @RequestParam(value = "from", required = false) Long from,
                                                                      @RequestParam(value = "to", required = false) Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportReportRecruitmentActivities u: {}", headerInfo);
        ExportResponse response = reportRecruitmentActivitiesService.exportReportRecruitmentActivities(from, to);
        File file = new File(response.getPath());
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=" + "ReportRecruitmentEfficiency.xlsx");
        logger.info("<=exportReportRecruitmentActivities u: {}, path: {}", headerInfo, path);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(httpHeaders)
                .body(resource);
    }
}
