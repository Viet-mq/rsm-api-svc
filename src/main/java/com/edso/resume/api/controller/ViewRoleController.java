package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ViewRoleEntity;
import com.edso.resume.api.domain.request.CreateViewRoleRequest;
import com.edso.resume.api.domain.request.DeleteViewRoleRequest;
import com.edso.resume.api.domain.request.UpdateViewRoleRequest;
import com.edso.resume.api.service.ViewRoleService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/view-role")
public class ViewRoleController extends BaseController {

    private final ViewRoleService viewRoleService;

    public ViewRoleController(ViewRoleService viewRoleService) {
        this.viewRoleService = viewRoleService;
    }

    @GetMapping("/list")
    public BaseResponse findAllViewRole(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllViewRole u: {}, id: {}, page: {}, size: {}", headerInfo, id, page, size);
        GetArrayResponse<ViewRoleEntity> resp = viewRoleService.findAll(headerInfo, id, name, page, size);
        logger.info("<=findAllViewRole u: {}, id: {}, page: {}, size: {}, resp: {}", headerInfo, id, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createViewRole(@RequestHeader Map<String, String> headers, @RequestBody CreateViewRoleRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createViewRole u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = viewRoleService.createViewRole(request);
            }
        }
        logger.info("<=createViewRole u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateViewRole(@RequestHeader Map<String, String> headers, @RequestBody UpdateViewRoleRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateViewRole u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = viewRoleService.updateViewRole(request);
            }
        }
        logger.info("<=updateViewRole u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteViewRole(@RequestHeader Map<String, String> headers, @RequestBody DeleteViewRoleRequest request) {
        logger.info("=>deleteViewRole req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = viewRoleService.deleteViewRole(request);
            }
        }
        logger.info("<=deleteViewRole req: {}, resp: {}", request, response);
        return response;
    }

}
