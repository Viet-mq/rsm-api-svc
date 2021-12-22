package com.edso.resume.api.controller;

import com.edso.resume.api.service.ParseEnglishService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParseEnglishController extends BaseController {

    private final ParseEnglishService parseEnglishService;

    public ParseEnglishController(ParseEnglishService parseEnglishService) {
        this.parseEnglishService = parseEnglishService;
    }

    @GetMapping("/parse")
    public void parse() {
        parseEnglishService.parseEnglish();
    }
}
