package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.AddCalendarsRequest;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest2;
import com.edso.resume.api.service.AddCalendarsService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/calendars")
public class AddCalendarsController extends BaseController {

    private final AddCalendarsService addCalendarsService;

    public AddCalendarsController(AddCalendarsService addCalendarsService) {
        this.addCalendarsService = addCalendarsService;
    }

    @PostMapping("/create")
    public BaseResponse addCalendars(@RequestHeader Map<String, String> headers, @RequestBody AddCalendarsRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>addCalendars u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = addCalendarsService.addCalendars(request);
            }
        }
        logger.info("<=addCalendars u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }
}
