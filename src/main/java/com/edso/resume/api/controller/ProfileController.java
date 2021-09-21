package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.ProfileService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController extends BaseController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/list")
    public BaseResponse findAllProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "fullName", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllProfile u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<ProfileEntity> resp = profileService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllProfile u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateProfileRequest request) throws ParseException {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
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

    @PostMapping("/update")
    public BaseResponse updateProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateProfile(request);
            }
        }
        logger.info("<=updateProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteProfileRequest request) {
        logger.info("=>deleteProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = profileService.deleteProfile(request);
            }
        }
        logger.info("<=deleteProfile req: {}, resp: {}", request, response);
        return response;
    }

    @PostMapping("/change-statsucv")
    public BaseResponse changeStatusCV(@RequestHeader Map<String, String> headers, @RequestBody ChangeStatusCVRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>changeStatusCV u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.changeStatusCV(request);
            }
        }
        logger.info("<=changeStatusCV u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }
}
