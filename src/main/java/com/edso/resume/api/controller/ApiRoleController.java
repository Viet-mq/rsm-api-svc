package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ApiRoleEntity;
import com.edso.resume.api.domain.request.CreateApiRoleRequest;
import com.edso.resume.api.domain.request.DeleteApiRoleRequest;
import com.edso.resume.api.domain.request.UpdateApiRoleRequest;
import com.edso.resume.api.service.ApiRoleService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api-role")
public class ApiRoleController extends BaseController {

    private final ApiRoleService apiRoleService;

    public ApiRoleController(ApiRoleService apiRoleService) {
        this.apiRoleService = apiRoleService;
    }


    @GetMapping("/list")
    public BaseResponse findAllApiRole(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllApiRole u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<ApiRoleEntity> resp = apiRoleService.findAll(headerInfo, id, name, page, size);
        logger.info("<=findAllApiRole u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createApiRole(@RequestHeader Map<String, String> headers, @RequestBody CreateApiRoleRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createApiRole u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = apiRoleService.createApiRole(request);
            }
        }
        logger.info("<=createApiRole u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateApiRole(@RequestHeader Map<String, String> headers, @RequestBody UpdateApiRoleRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateApiRole u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = apiRoleService.updateApiRole(request);
            }
        }
        logger.info("<=updateApiRole u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteApiRole(@RequestHeader Map<String, String> headers, @RequestBody DeleteApiRoleRequest request) {
        logger.info("=>deleteApiRole req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = apiRoleService.deleteApiRole(request);
            }
        }
        logger.info("<=deleteApiRole req: {}, resp: {}", request, response);
        return response;
    }

}
