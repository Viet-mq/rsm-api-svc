package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.Profile;
import com.edso.resume.api.domain.request.CreateProfileRequest;
import com.edso.resume.api.service.ProfileService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController extends BaseController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/list")
    public GetArrayResponse<Profile> getProfiles(@RequestHeader Map<String, String> headers) {
        return null;
    }

    @PostMapping("/create")
    public BaseResponse createProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createProfile u: {}, req: {}", headerInfo, request);
        if (request == null || !headerInfo.validate()) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.createProfile(request);
            }
        }
        logger.info("<=createProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

}
