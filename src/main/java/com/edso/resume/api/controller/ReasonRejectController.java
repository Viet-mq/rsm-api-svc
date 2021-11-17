package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReasonRejectEntity;
import com.edso.resume.api.domain.request.CreateReasonRejectRequest;
import com.edso.resume.api.domain.request.DeleteReasonRejectRequest;
import com.edso.resume.api.domain.request.UpdateReasonRejectRequest;
import com.edso.resume.api.service.ReasonRejectService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reason")
public class ReasonRejectController extends BaseController {
    private final ReasonRejectService reasonRejectService;

    public ReasonRejectController(ReasonRejectService reasonRejectService) {
        this.reasonRejectService = reasonRejectService;
    }

    @GetMapping("/list")
    public BaseResponse findAllReasonReject(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllReasonReject u: {}, page: {}, size: {}", headerInfo, page, size);
        GetArrayResponse<ReasonRejectEntity> resp = reasonRejectService.findAll(headerInfo, page, size);
        logger.info("<=findAllReasonReject u: {}, page: {}, size: {}, resp: {}", headerInfo, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createReasonReject(@RequestHeader Map<String, String> headers, @RequestBody CreateReasonRejectRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createReasonReject u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = reasonRejectService.createReasonReject(request);
            }
        }
        logger.info("<=createReasonReject u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateReasonReject(@RequestHeader Map<String, String> headers, @RequestBody UpdateReasonRejectRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateReasonReject u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = reasonRejectService.updateReasonReject(request);
            }
        }
        logger.info("<=updateReasonReject u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteReasonReject(@RequestHeader Map<String, String> headers, @RequestBody DeleteReasonRejectRequest request) {
        logger.info("=>deleteReasonReject req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = reasonRejectService.deleteReasonReject(request);
            }
        }
        logger.info("<=deleteReasonReject req: {}, resp: {}", request, response);
        return response;
    }
}
