package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.ChangeRecruitmentRequest;
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
    public BaseResponse changeRecruitment(@RequestHeader Map<String, String> headers, @RequestBody ChangeRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>changeRecruitment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = changeRecruitmentService.changeRecruitment(request);
            }
        }
        logger.info("<=changeRecruitment u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }
}
