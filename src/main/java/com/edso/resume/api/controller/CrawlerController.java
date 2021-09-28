package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.CreateCrawlerRequest;
import com.edso.resume.api.service.CrawlerService;
import com.edso.resume.lib.response.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/crawler")
public class CrawlerController extends BaseController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/create")
    public BaseResponse createCrawlApi(
            @RequestHeader Map<String, String> headers,
            @RequestBody CreateCrawlerRequest request) {

    }

    @PostMapping("/update")
    public BaseResponse updateCrawlApi(
            @RequestHeader Map<String, String> headers,
            @RequestBody CreateCrawlerRequest request) {

    }

}
