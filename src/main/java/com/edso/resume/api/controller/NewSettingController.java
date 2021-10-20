package com.edso.resume.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/new-setting")
public class NewSettingController extends BaseController {
    @GetMapping("/setting")
    public String setting() {
        return "/setting";
    }
}
