package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.SendOutlookCalendarRequest;
import com.edso.resume.api.service.SendOutlookCalendarService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/calendar")
public class SendOutlookCalendarController extends BaseController{
    private final SendOutlookCalendarService sendOutlookCalendarService;

    public SendOutlookCalendarController(SendOutlookCalendarService sendOutlookCalendarService) {
        this.sendOutlookCalendarService = sendOutlookCalendarService;
    }

    @PostMapping("/send")
    public BaseResponse sendOutlookCalendar(@RequestHeader Map<String, String> headers, @RequestBody SendOutlookCalendarRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>sendOutlookCalendar u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = sendOutlookCalendarService.sendOutlookCalendar(request);
            }
        }
        logger.info("<=sendOutlookCalendar u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }
}
