package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.JobLevelEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.JobLevelService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//level: intern, junior, ......
@RestController
@RequestMapping("/joblevel")
public class JobLevelController extends BaseController {

    private final JobLevelService jobLevelService;

    public JobLevelController(JobLevelService jobLevelService) {
        this.jobLevelService = jobLevelService;
    }

    @GetMapping("/list")
    public BaseResponse findAllJobLevel(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAll u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<JobLevelEntity> resp = jobLevelService.findAll(headerInfo, name, page, size);
        logger.info("<=findAll u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createJobLevel(@RequestHeader Map<String, String> headers, @RequestBody CreateJobLevelRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createJobLevel u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = jobLevelService.createJobLevel(request);
            }
        }
        logger.info("<=createJobLevel u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateJobLevel(@RequestHeader Map<String, String> headers, @RequestBody UpdateJobLevelRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateJobLevel u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = jobLevelService.updateJobLevel(request);
            }
        }
        logger.info("<=updateJobLevel u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteJobLevel(@RequestHeader Map<String, String> headers, @RequestBody DeleteJobLevelRequest request) {
        logger.info("=>deleteJobLevel req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = jobLevelService.deleteJobLevel(request);
            }
        }
        logger.info("<=deleteJobLevel req: {}, resp: {}", request, response);
        return response;
    }

}