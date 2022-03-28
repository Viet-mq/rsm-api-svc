package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.PermissionEntity;
import com.edso.resume.api.domain.request.CreatePermissionRequest;
import com.edso.resume.api.domain.request.DeletePermissionRequest;
import com.edso.resume.api.domain.request.UpdatePermissionRequest;
import com.edso.resume.api.service.PermissionService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/permission")
public class PermissionController extends BaseController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/list")
    public BaseResponse findAllPermission(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllPermission u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<PermissionEntity> resp = permissionService.findAll(headerInfo, id, name, page, size);
        logger.info("<=findAllPermission u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createPermission(@RequestHeader Map<String, String> headers, @RequestBody CreatePermissionRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createPermission u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = permissionService.createPermission(request);
            }
        }
        logger.info("<=createPermission u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updatePermission(@RequestHeader Map<String, String> headers, @RequestBody UpdatePermissionRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updatePermission u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = permissionService.updatePermission(request);
            }
        }
        logger.info("<=updatePermission u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deletePermission(@RequestHeader Map<String, String> headers, @RequestBody DeletePermissionRequest request) {
        logger.info("=>deletePermission req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = permissionService.deletePermission(request);
            }
        }
        logger.info("<=deletePermission req: {}, resp: {}", request, response);
        return response;
    }

}
