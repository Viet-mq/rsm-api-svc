package com.edso.resume.api.controller;

import com.edso.resume.api.service.UploadProfilesService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/profiles")
public class ListProfileController extends BaseController {

    public final UploadProfilesService uploadProfilesService;

    public ListProfileController(UploadProfilesService uploadProfilesService) {
        this.uploadProfilesService = uploadProfilesService;
    }


    @PostMapping(value = "/upload")
    public BaseResponse uploadProfiles(
            @RequestHeader Map<String, String> headers,
            @RequestParam("file") MultipartFile file) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>uploadProfiles u: {}", headerInfo);
        BaseResponse response = uploadProfilesService.uploadProfiles(file, headerInfo);
        logger.info("<=uploadProfiles u: {}, rep: {}", headerInfo, response);
        return response;
    }
}
