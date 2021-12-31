package com.edso.resume.api.controller;

import com.edso.resume.api.domain.response.StatusCVResponse;
import com.edso.resume.api.service.CreateIdForStatusCVService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class CreateIdForStatusCVController {
    private final CreateIdForStatusCVService createIdForStatusCVService;

    public CreateIdForStatusCVController(CreateIdForStatusCVService createIdForStatusCVService) {
        this.createIdForStatusCVService = createIdForStatusCVService;
    }

    @PostMapping("/create")
    StatusCVResponse createIdForStatusCV(@RequestParam("name") String name) {
        return createIdForStatusCVService.createIdForStatusCV(name);
    }
}
