package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.StatusCVEntity;
import com.edso.resume.api.domain.request.CreateStatusCVRequest;
import com.edso.resume.api.domain.request.DeleteStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateAllStatusCVRequest;
import com.edso.resume.api.domain.request.UpdateStatusCVRequest;
import com.edso.resume.api.service.StatusCVService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statuscv")
public class StatusCVController extends BaseController {

    private final StatusCVService statusCVService;

    public StatusCVController(StatusCVService statusCVService) {
        this.statusCVService = statusCVService;
    }

    @GetMapping("/list")
    public BaseResponse findAllStatusCV(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllStatusCV u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<StatusCVEntity> resp = statusCVService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllStatusCV u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createStatusCV(@RequestHeader Map<String, String> headers,
                                       @RequestBody CreateStatusCVRequest request,
                                       @RequestParam(value = "children", required = false) List<String> children) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createStatusCV u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = statusCVService.createStatusCV(request, children);
            }
        }
        logger.info("<=createStatusCV u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateStatusCV(@RequestHeader Map<String, String> headers,
                                       @RequestBody UpdateStatusCVRequest request,
                                       @RequestParam(value = "children", required = false) List<String> children) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateStatusCV u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = statusCVService.updateStatusCV(request, children);
            }
        }
        logger.info("<=updateStatusCV u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update-all")
    public BaseResponse updateStatusCV(@RequestHeader Map<String, String> headers,
                                       @RequestBody UpdateAllStatusCVRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateStatusCV u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = statusCVService.updateAllStatusCV(request);
            }
        }
        logger.info("<=updateStatusCV u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteStatusCV(@RequestHeader Map<String, String> headers, @RequestBody DeleteStatusCVRequest request) {
        logger.info("=>deleteStatusCV req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = statusCVService.deleteStatusCV(request);
            }
        }
        logger.info("<=deleteStatusCV req: {}, resp: {}", request, response);
        return response;
    }

}
