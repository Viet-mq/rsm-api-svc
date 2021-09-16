package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.request.CreateJobRequest;
import com.edso.resume.api.domain.request.DeleteJobRequest;
import com.edso.resume.api.domain.request.UpdateJobRequest;
import com.edso.resume.api.service.JobService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/job")
public class JobController extends BaseController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/list")
    public BaseResponse findAll(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAll u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<CategoryEntity> resp = jobService.findAll(headerInfo, name, page, size);
        logger.info("<=findAll u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createAccount(@RequestHeader Map<String, String> headers, @RequestBody CreateJobRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createJob u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = jobService.createJob(request);
            }
        }
        logger.info("<=createJob u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateAccount(@RequestHeader Map<String, String> headers, @RequestBody UpdateJobRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateJob u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = jobService.updateJob(request);
            }
        }
        logger.info("<=updateJob u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteApiRole(@RequestHeader Map<String, String> headers, @RequestBody DeleteJobRequest request) {
        logger.info("=>deleteJob req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = jobService.deleteJob(request);
            }
        }
        logger.info("<=deleteJob req: {}, resp: {}", request, response);
        return response;
    }

}
