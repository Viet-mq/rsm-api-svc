package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CategoryEntity;
import com.edso.resume.api.domain.entities.SourceCVEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.JobService;
import com.edso.resume.api.service.SourceCVService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//Nguon: facebook, topCV, ref
@RestController
@RequestMapping("/sourcecv")
public class SourceCVController extends BaseController {

    private final SourceCVService sourceCVService;

    public SourceCVController(SourceCVService sourceCVService) {
        this.sourceCVService = sourceCVService;
    }

    @GetMapping("/list")
    public BaseResponse findAllSourceCV(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAll u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<SourceCVEntity> resp = sourceCVService.findAll(headerInfo, name, page, size);
        logger.info("<=findAll u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createSourceCV(@RequestHeader Map<String, String> headers, @RequestBody CreateSourceCVRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createSourceCV u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = sourceCVService.createSourceCV(request);
            }
        }
        logger.info("<=createSourceCV u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateSourceCV(@RequestHeader Map<String, String> headers, @RequestBody UpdateSourceCVRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateSourceCV u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = sourceCVService.updateSourceCV(request);
            }
        }
        logger.info("<=updateSourceCV u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteSourceCV(@RequestHeader Map<String, String> headers, @RequestBody DeleteSourceCVRequest request) {
        logger.info("=>deleteSourceCV req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = sourceCVService.deleteSourceCV(request);
            }
        }
        logger.info("<=deleteSourceCV req: {}, resp: {}", request, response);
        return response;
    }

}

