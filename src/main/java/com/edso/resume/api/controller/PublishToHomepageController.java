package com.edso.resume.api.controller;

import com.edso.resume.api.service.PublishToHomepageService;
import com.edso.resume.lib.response.BaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("homepage")
public class PublishToHomepageController extends BaseController {

    private final PublishToHomepageService publishToHomepageService;

    public PublishToHomepageController(PublishToHomepageService publishToHomepageService) {
        this.publishToHomepageService = publishToHomepageService;
    }

    @PostMapping("/publish")
    public BaseResponse publishToHomepage() {
        BaseResponse response = new BaseResponse();
        response = publishToHomepageService.publishToHomepage();
        return response;
    }
}
