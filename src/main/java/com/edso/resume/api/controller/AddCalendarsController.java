package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.AddCalendarsRequest;
import com.edso.resume.api.domain.request.CandidateRequest;
import com.edso.resume.api.domain.request.PresenterRequest;
import com.edso.resume.api.domain.request.RecruitmentCouncilRequest;
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
    public BaseResponse addCalendars(@RequestHeader Map<String, String> headers,
                                     @ModelAttribute AddCalendarsRequest request,
                                     @ModelAttribute PresenterRequest presenter,
                                     @ModelAttribute RecruitmentCouncilRequest recruitmentCouncil,
                                     @ModelAttribute CandidateRequest candidate) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>addCalendars u: {}, req: {}, presenter: {}, recruitmentCouncil: {}, candidate: {}", headerInfo, request, presenter, recruitmentCouncil, candidate);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = addCalendarsService.addCalendars(request, presenter, recruitmentCouncil, candidate);
            }
        }
        logger.info("<=addCalendars u: {}, req: {}, resp: {}, presenter: {}, recruitmentCouncil: {}, candidate: {}", headerInfo, request, response, presenter, recruitmentCouncil, candidate);
        return response;
    }
}
