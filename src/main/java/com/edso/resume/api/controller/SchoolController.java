package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.entities.SchoolEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.JobService;
import com.edso.resume.api.service.SchoolService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//School: HaUI, PTIT, .....
@RestController
@RequestMapping("/school")
public class SchoolController extends BaseController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping("/list")
    public BaseResponse findAllSchool(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAll u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<SchoolEntity> resp = schoolService.findAll(headerInfo, name, page, size);
        logger.info("<=findAll u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createSchool(@RequestHeader Map<String, String> headers, @RequestBody CreateSchoolRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createSchool u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = schoolService.createSchool(request);
            }
        }
        logger.info("<=createSchool u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateSchool(@RequestHeader Map<String, String> headers, @RequestBody UpdateSchoolRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateSchool u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = schoolService.updateSchool(request);
            }
        }
        logger.info("<=updateSchool u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteSchool(@RequestHeader Map<String, String> headers, @RequestBody DeleteSchoolRequest request) {
        logger.info("=>deleteSchool req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = schoolService.deleteSchool(request);
            }
        }
        logger.info("<=deleteSchool req: {}, resp: {}", request, response);
        return response;
    }

}
