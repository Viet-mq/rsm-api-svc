package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.DepartmentEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.DepartmentService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Phong ban: R&D, SS, BDA
@RestController
@RequestMapping("/department")
public class DepartmentController extends BaseController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/list")
    public BaseResponse findAllDeparment(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAll u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<DepartmentEntity> resp = departmentService.findAll(headerInfo, name, page, size);
        logger.info("<=findAll u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createDepartment(@RequestHeader Map<String, String> headers, @RequestBody CreateDepartmentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createDepartment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = departmentService.createDepartment(request);
            }
        }
        logger.info("<=createDepartment u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateDepartment(@RequestHeader Map<String, String> headers, @RequestBody UpdateDepartmentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateDepartment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = departmentService.updateDepartment(request);
            }
        }
        logger.info("<=updateDepartment u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteDepartment(@RequestHeader Map<String, String> headers, @RequestBody DeleteDepartmentRequest request) {
        logger.info("=>deleteDepartment req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = departmentService.deleteDepartment(request);
            }
        }
        logger.info("<=deleteDepartment req: {}, resp: {}", request, response);
        return response;
    }
}
