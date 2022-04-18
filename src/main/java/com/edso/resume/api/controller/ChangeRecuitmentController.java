package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.ChangeRecruitmentService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ChangeRecuitmentController extends BaseController {

    private final ChangeRecruitmentService changeRecruitmentService;

    public ChangeRecuitmentController(ChangeRecruitmentService changeRecruitmentService) {
        this.changeRecruitmentService = changeRecruitmentService;
    }

    @PostMapping("/change")
    public BaseResponse changeRecruitment(@RequestHeader Map<String, String> headers,
                                          @ModelAttribute ChangeRecruitmentRequest request,
                                          @ModelAttribute RecruitmentCouncilRequest recruitmentCouncil,
                                          @ModelAttribute CandidateRequest candidate,
                                          @ModelAttribute RelatedPeopleRequest relatedPeople,
                                          @ModelAttribute PresenterRequest presenter) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>changeRecruitment u: {}, req: {}, candidate: {}, presenter: {}, relatedPeople: {}, recruitmentCouncil: {}", headerInfo, request, candidate, presenter, relatedPeople, recruitmentCouncil);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = changeRecruitmentService.changeRecruitment(request, candidate, presenter, relatedPeople, recruitmentCouncil);
            }
        }
        logger.info("<=changeRecruitment u: {}, req: {}, resp: {}, candidate: {}, presenter: {}, relatedPeople: {}, recruitmentCouncil: {}", headerInfo, request, response, candidate, presenter, relatedPeople, recruitmentCouncil);
        return response;
    }
}
