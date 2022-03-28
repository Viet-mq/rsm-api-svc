package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ApiEntity;
import com.edso.resume.api.domain.request.CreateApiRequest;
import com.edso.resume.api.domain.request.DeleteApiRequest;
import com.edso.resume.api.domain.request.UpdateApiRequest;
import com.edso.resume.api.service.ApiService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController extends BaseController {

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }


    @GetMapping("/list")
    public BaseResponse findAllApi(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllApi u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<ApiEntity> resp = apiService.findAll(headerInfo, id, name, page, size);
        logger.info("<=findAllApi u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createApi(@RequestHeader Map<String, String> headers, @RequestBody CreateApiRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createApi u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = apiService.createApi(request);
            }
        }
        logger.info("<=createApi u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateApi(@RequestHeader Map<String, String> headers, @RequestBody UpdateApiRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateApi u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = apiService.updateApi(request);
            }
        }
        logger.info("<=updateApi u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteApi(@RequestHeader Map<String, String> headers, @RequestBody DeleteApiRequest request) {
        logger.info("=>deleteApi req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = apiService.deleteApi(request);
            }
        }
        logger.info("<=deleteApi req: {}, resp: {}", request, response);
        return response;
    }

}
