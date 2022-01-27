package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.CreateIdForStatusCVRequest;
import com.edso.resume.api.domain.response.StatusCVResponse;
import com.edso.resume.api.service.CreateIdForStatusCVService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class CreateIdForStatusCVController extends BaseController {
    private final CreateIdForStatusCVService createIdForStatusCVService;

    public CreateIdForStatusCVController(CreateIdForStatusCVService createIdForStatusCVService) {
        this.createIdForStatusCVService = createIdForStatusCVService;
    }

    @PostMapping("/create")
    public StatusCVResponse createIdForStatusCV(@RequestBody CreateIdForStatusCVRequest request) {
        StatusCVResponse response = new StatusCVResponse();
        logger.info("=>createIdForStatusCV req: {}", request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                response = createIdForStatusCVService.createIdForStatusCV(request);
            }
        }
        logger.info("<=createIdForStatusCV req: {}, resp: {}", request, response);
        return response;
    }
}
