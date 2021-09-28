package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.request.CreateCalendarProfileRequest;
import com.edso.resume.api.domain.request.DeleteCalendarProfileRequest;
import com.edso.resume.api.domain.request.UpdateCalendarProfileRequest;
import com.edso.resume.api.service.CalendarService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarReponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/calendar")
public class CalendarController extends BaseController{

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService){
        this.calendarService = calendarService;
    }

    @GetMapping("/list")
    public BaseResponse findAllCalendar(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile", required = false) String idProfile) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllCalendar u: {}, idProfile: {}", headerInfo, idProfile);
        GetArrayCalendarReponse<CalendarEntity> resp = calendarService.findAllCalendar(headerInfo, idProfile);
        logger.info("<=findAllCalendar u: {}, idProfile: {}, resp: {}", headerInfo, idProfile, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createCalendarProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createCalendarProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = calendarService.createCalendarProfile(request);
            }
        }
        logger.info("<=createCalendarProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateCalendarProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateCalendarProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = calendarService.updateCalendarProfile(request);
            }
        }
        logger.info("<=updateCalendarProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteCalendarProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteCalendarProfileRequest request) {
        logger.info("=>deleteCalendarProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = calendarService.deleteCalendarProfile(request);
            }
        }
        logger.info("<=deleteCalendarProfile req: {}, resp: {}", request, response);
        return response;
    }
}
