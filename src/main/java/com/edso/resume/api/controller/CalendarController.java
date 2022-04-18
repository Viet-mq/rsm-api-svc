package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CalendarEntity2;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.CalendarService;
import com.edso.resume.api.service.CalendarService2;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/calendar")
public class CalendarController extends BaseController {

    private final CalendarService calendarService;
    private final CalendarService2 calendarService2;

    public CalendarController(CalendarService calendarService, CalendarService2 calendarService2) {
        this.calendarService = calendarService;
        this.calendarService2 = calendarService2;
    }


    @GetMapping("/list")
    public BaseResponse findAllCalendar(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "act", required = false) String act,
            @RequestParam(value = "keySearch", required = false) String keySearch,
            @RequestParam(value = "recruitment", required = false) String recruitment,
            @RequestParam(value = "idProfile", required = false) String idProfile) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllCalendar u: {}, id: {}, key: {}, act: {}, keySearch: {}, recruitment: {}, idProfile: {}", headerInfo, id, key, act, keySearch, recruitment, idProfile);
        GetArrayCalendarResponse<CalendarEntity2> resp = calendarService2.findAllCalendar(headerInfo, id, idProfile, key, act, keySearch, recruitment);
        logger.info("<=findAllCalendar u: {}, id: {}, key: {}, act: {}, keySearch: {}, recruitment:{}, idProfile: {}, resp: {}", headerInfo, id, key, act, keySearch, recruitment, idProfile, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createCalendarProfile(@RequestHeader Map<String, String> headers,
                                              @ModelAttribute CreateCalendarProfileRequest2 request,
                                              @ModelAttribute PresenterRequest presenter,
                                              @ModelAttribute RecruitmentCouncilRequest recruitmentCouncil,
                                              @ModelAttribute RelatedPeopleRequest relatedPeople,
                                              @ModelAttribute CandidateRequest candidate) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createCalendarProfile u: {}, req: {}, presenter: {}, recruitmentCouncil: {}, candidate: {}, relatedPeople: {}", headerInfo, request, presenter, recruitmentCouncil, candidate, relatedPeople);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = calendarService2.createCalendarProfile(request, presenter, recruitmentCouncil, candidate, relatedPeople);
            }
        }
        logger.info("<=createCalendarProfile u: {}, req: {}, rep: {}, presenter: {}, recruitmentCouncil: {}, candidate: {}, relatedPeople: {}", headerInfo, request, response, presenter, recruitmentCouncil, candidate, relatedPeople);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateCalendarProfile(@RequestHeader Map<String, String> headers,
                                              @ModelAttribute UpdateCalendarProfileRequest2 request,
                                              @ModelAttribute PresenterRequest presenter,
                                              @ModelAttribute RecruitmentCouncilRequest recruitmentCouncil,
                                              @ModelAttribute RelatedPeopleRequest relatedPeople,
                                              @ModelAttribute CandidateRequest candidate) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateCalendarProfile u: {}, req: {}, presenter: {}, recruitmentCouncil: {}, candidate: {}, relatedPeople: {}", headerInfo, request, presenter, recruitmentCouncil, candidate, relatedPeople);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = calendarService2.updateCalendarProfile(request, presenter, recruitmentCouncil, candidate, relatedPeople);
            }
        }
        logger.info("<=updateCalendarProfile u: {}, req: {}, resp: {}, presenter: {}, recruitmentCouncil: {}, candidate: {}, relatedPeople: {}", headerInfo, request, response, presenter, recruitmentCouncil, candidate, relatedPeople);
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
                response = calendarService2.deleteCalendarProfile(request);
            }
        }
        logger.info("<=deleteCalendarProfile req: {}, resp: {}", request, response);
        return response;
    }

    //    @Scheduled(fixedRate = 60000)
    public void alarmInterview() {
        calendarService2.alarmInterview();
    }

}
