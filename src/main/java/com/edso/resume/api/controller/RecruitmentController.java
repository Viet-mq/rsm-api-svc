package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.RecruitmentEntity;
import com.edso.resume.api.domain.request.CreateRecruitmentRequest;
import com.edso.resume.api.domain.request.DeleteRecruitmentRequest;
import com.edso.resume.api.domain.request.UpdateRecruitmentRequest;
import com.edso.resume.api.domain.response.GetRecruitmentResponse;
import com.edso.resume.api.service.RecruitmentService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/recruitment")
public class RecruitmentController extends BaseController {
    private final RecruitmentService recruitmentService;

    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    @GetMapping("/list")
    public BaseResponse findAllRecruitment(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllRecruitment u: {}, page: {}, size: {}", headerInfo, page, size);
        GetArrayResponse<RecruitmentEntity> resp = recruitmentService.findAll(headerInfo, page, size);
        logger.info("<=findAllRecruitment u: {}, page: {}, size: {}, resp: {}", headerInfo, page, size, resp.info());
        return resp;
    }

    @GetMapping("/detail")
    public BaseResponse findOneRecruitment(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "recruitmentId") String recruitmentId) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findOneRecruitment u: {}, recruitmentId: {}", headerInfo, recruitmentId);
        GetRecruitmentResponse<RecruitmentEntity> resp = recruitmentService.findOne(headerInfo, recruitmentId);
        logger.info("<=findOneRecruitment u: {}, recruitmentId: {}, resp: {}", headerInfo, recruitmentId, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createRecruitment(@RequestHeader Map<String, String> headers, @RequestBody CreateRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createRecruitment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = recruitmentService.createRecruitment(request);
            }
        }
        logger.info("<=createRecruitment u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateRecruitment(@RequestHeader Map<String, String> headers, @RequestBody UpdateRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateRecruitment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = recruitmentService.updateRecruitment(request);
            }
        }
        logger.info("<=updateRecruitment u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteRecruitment(@RequestHeader Map<String, String> headers, @RequestBody DeleteRecruitmentRequest request) {
        logger.info("=>deleteRecruitment req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = recruitmentService.deleteRecruitment(request);
            }
        }
        logger.info("<=deleteRecruitment req: {}, resp: {}", request, response);
        return response;
    }
}
