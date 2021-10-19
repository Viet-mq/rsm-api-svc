package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.TalentPoolService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("talent-pool/")
public class TalentPoolController extends BaseController{

    private final TalentPoolService talentPoolService;

    public TalentPoolController(TalentPoolService talentPoolService) {
        this.talentPoolService = talentPoolService;
    }

    @GetMapping("/list")
    public BaseResponse findAllTalentPool(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllTalentPool u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<TalentPoolEntity> resp = talentPoolService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllTalentPool u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createTalentPool(@RequestHeader Map<String, String> headers, @RequestBody CreateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createTalentPool u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = talentPoolService.createTalentPool(request);
            }
        }
        logger.info("<=createTalentPool u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateTalentPool(@RequestHeader Map<String, String> headers, @RequestBody UpdateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateTalentPool u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = talentPoolService.updateTalentPool(request);
            }
        }
        logger.info("<=updateTalentPool u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteTalentPool(@RequestHeader Map<String, String> headers, @RequestBody DeleteTalentPoolRequest request) {
        logger.info("=>deleteTalentPool req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = talentPoolService.deleteTalentPool(request);
            }
        }
        logger.info("<=deleteTalentPool req: {}, resp: {}", request, response);
        return response;
    }

}


