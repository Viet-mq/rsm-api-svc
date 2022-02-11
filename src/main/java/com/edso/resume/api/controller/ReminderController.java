package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ReminderEntity;
import com.edso.resume.api.domain.request.CreateReminderRequest;
import com.edso.resume.api.domain.request.DeleteReminderRequest;
import com.edso.resume.api.domain.request.UpdateReminderRequest;
import com.edso.resume.api.service.ReminderService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reminder")
public class ReminderController extends BaseController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/list")
    public BaseResponse findAll(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "from") Long from,
            @RequestParam(value = "to") Long to) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllReminder u: {}, from: {}, to: {}", headerInfo, from, to);
        GetArrayResponse<ReminderEntity> resp = reminderService.findAll(headerInfo, from, to);
        logger.info("<=findAllReminder u: {}, from: {}, to: {}, resp: {}", headerInfo, from, to, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createReminder(@RequestHeader Map<String, String> headers, @RequestBody CreateReminderRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createReminder u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = reminderService.createReminder(request);
            }
        }
        logger.info("<=createReminder u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateReminder(@RequestHeader Map<String, String> headers, @RequestBody UpdateReminderRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateReminder u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = reminderService.updateReminder(request);
            }
        }
        logger.info("<=updateReminder u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteReminder(@RequestHeader Map<String, String> headers, @RequestBody DeleteReminderRequest request) {
        logger.info("=>deleteReminder req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = reminderService.deleteReminder(request);
            }
        }
        logger.info("<=deleteReminder req: {}, resp: {}", request, response);
        return response;
    }
}
