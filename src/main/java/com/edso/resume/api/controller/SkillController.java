package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.SkillEntity;
import com.edso.resume.api.domain.request.CreateSkillRequest;
import com.edso.resume.api.domain.request.DeleteSkillRequest;
import com.edso.resume.api.domain.request.UpdateSkillRequest;
import com.edso.resume.api.service.SkillService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/skill")
public class SkillController extends BaseController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/list")
    public BaseResponse findAllSkill(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllSkill u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<SkillEntity> resp = skillService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllSkill u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createSkill(@RequestHeader Map<String, String> headers, @RequestBody CreateSkillRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createSkill u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = skillService.createSkill(request);
            }
        }
        logger.info("<=createSkill u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateSkill(@RequestHeader Map<String, String> headers, @RequestBody UpdateSkillRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateSkill u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = skillService.updateSkill(request);
            }
        }
        logger.info("<=updateSkill u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteSkill(@RequestHeader Map<String, String> headers, @RequestBody DeleteSkillRequest request) {
        logger.info("=>deleteSkill req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = skillService.deleteSkill(request);
            }
        }
        logger.info("<=deleteSkill req: {}, resp: {}", request, response);
        return response;
    }
}
