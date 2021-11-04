package com.edso.resume.api.controller;

import com.edso.resume.api.domain.request.DeleteImageProfileRequest;
import com.edso.resume.api.domain.request.UpdateImageProfileRequest;
import com.edso.resume.api.service.ImageProfileService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/image")
public class ImageProfileController extends BaseController {

    private final ImageProfileService imageProfileService;

    public ImageProfileController(ImageProfileService imageProfileService) {
        this.imageProfileService = imageProfileService;
    }

    @PostMapping("/update")
    public BaseResponse updateImageProfile(@RequestHeader Map<String, String> headers, @RequestParam("image") MultipartFile image, @RequestParam("idProfile") String idProfile) {
        UpdateImageProfileRequest request = new UpdateImageProfileRequest(idProfile, image);
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateImageProfile u: {}, req: {}", headerInfo, request);
        BaseResponse response = request.validate();
        if (response == null) {
            request.setInfo(headerInfo);
            response = imageProfileService.updateImageProfile(request);
        }
        logger.info("<=updateImageProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteImageProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteImageProfileRequest request) {
        logger.info("=>deleteImageProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = imageProfileService.deleteImageProfile(request);
            }
        }
        logger.info("<=deleteImageProfile req: {}, resp: {}", request, response);
        return response;
    }
}
