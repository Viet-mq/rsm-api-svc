package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.OrganizationEntity;
import com.edso.resume.api.domain.request.CreateOrganizationRequest;
import com.edso.resume.api.domain.request.DeleteOrganizationRequest;
import com.edso.resume.api.domain.request.UpdateOrganizationRequest;
import com.edso.resume.api.service.OrganizationService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/organization")
public class OrganizationController extends BaseController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/list")
    public BaseResponse findAllOrganization(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllOrganization u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<OrganizationEntity> resp = organizationService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllOrganization u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createOrganization(@RequestHeader Map<String, String> headers, @RequestBody CreateOrganizationRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createOrganization u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = organizationService.createOrganization(request);
            }
        }
        logger.info("<=createOrganization u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateOrganization(@RequestHeader Map<String, String> headers, @RequestBody UpdateOrganizationRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateOrganization u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = organizationService.updateOrganization(request);
            }
        }
        logger.info("<=updateOrganization u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteOrganization(@RequestHeader Map<String, String> headers, @RequestBody DeleteOrganizationRequest request) {
        logger.info("=>deleteOrganization req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = organizationService.deleteOrganization(request);
            }
        }
        logger.info("<=deleteOrganization req: {}, resp: {}", request, response);
        return response;
    }

}