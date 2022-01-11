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
public class CreateIdForStatusCVController {
    private final CreateIdForStatusCVService createIdForStatusCVService;

    public CreateIdForStatusCVController(CreateIdForStatusCVService createIdForStatusCVService) {
        this.createIdForStatusCVService = createIdForStatusCVService;
    }

    @PostMapping("/create")
    StatusCVResponse createIdForStatusCV(@RequestBody CreateIdForStatusCVRequest request) {
        return createIdForStatusCVService.createIdForStatusCV(request);
    }
}
