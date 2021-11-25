package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.service.ReportByDepartmentService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController extends BaseController {
    private final ReportByDepartmentService reportByDepartmentService;

    public ReportController(ReportByDepartmentService reportByDepartmentService) {
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
}
