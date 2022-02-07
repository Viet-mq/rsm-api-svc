package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.SendEmailRequest;
import com.edso.resume.api.service.SendEmailService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
public class SendEmailController extends BaseController {
    private final SendEmailService sendEmailService;

    public SendEmailController(SendEmailService sendEmailService) {
        this.sendEmailService = sendEmailService;
    }

    @PostMapping("/send")
    public BaseResponse sendEmail(@RequestHeader Map<String, String> headers, @RequestBody SendEmailRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>sendEmail u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = sendEmailService.sendEmail(request);
            }
        }
        logger.info("<=sendEmail u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }
}
