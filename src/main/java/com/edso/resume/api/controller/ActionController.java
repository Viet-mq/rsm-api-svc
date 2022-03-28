package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.CreateActionRequest;
import com.edso.resume.api.domain.request.DeleteActionRequest;
import com.edso.resume.api.domain.request.UpdateActionRequest;
import com.edso.resume.api.service.ActionService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/action")
public class ActionController extends BaseController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @PostMapping("/create")
    public BaseResponse createAction(@RequestHeader Map<String, String> headers, @RequestBody CreateActionRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createAction u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = actionService.createAction(request);
            }
        }
        logger.info("<=createAction u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateAction(@RequestHeader Map<String, String> headers, @RequestBody UpdateActionRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateAction u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = actionService.updateAction(request);
            }
        }
        logger.info("<=updateAction u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteAction(@RequestHeader Map<String, String> headers, @RequestBody DeleteActionRequest request) {
        logger.info("=>deleteAction req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = actionService.deleteAction(request);
            }
        }
        logger.info("<=deleteAction req: {}, resp: {}", request, response);
        return response;
    }

}