package com.edso.resume.api.controller;


import com.edso.resume.api.domain.entities.RoleEntity;
import com.edso.resume.api.domain.request.CreateRoleRequest;
import com.edso.resume.api.domain.request.DeleteRoleRequest;
import com.edso.resume.api.domain.request.UpdateRoleRequest;
import com.edso.resume.api.service.RoleService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/role")
public class RoleController extends BaseController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    public BaseResponse findAllRole(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllRole u: {}, id: {}, page: {}, size: {}", headerInfo, id, page, size);
        GetArrayResponse<RoleEntity> resp = roleService.findAll(headerInfo, id, name, page, size);
        logger.info("<=findAllRole u: {}, id: {}, page: {}, size: {}, resp: {}", headerInfo, id, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createRole(@RequestHeader Map<String, String> headers, @RequestBody CreateRoleRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createRole u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = roleService.createRole(request);
            }
        }
        logger.info("<=createRole u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateRole(@RequestHeader Map<String, String> headers, @RequestBody UpdateRoleRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateRole u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = roleService.updateRole(request);
            }
        }
        logger.info("<=updateRole u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteRole(@RequestHeader Map<String, String> headers, @RequestBody DeleteRoleRequest request) {
        logger.info("=>deleteRole req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = roleService.deleteRole(request);
            }
        }
        logger.info("<=deleteRole req: {}, resp: {}", request, response);
        return response;
    }

}
