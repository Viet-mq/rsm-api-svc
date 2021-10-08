package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.BlacklistEntity;
import com.edso.resume.api.domain.request.CreateBlacklistRequest;
import com.edso.resume.api.domain.request.DeleteBlacklistRequest;
import com.edso.resume.api.domain.request.UpdateBlacklistRequest;
import com.edso.resume.api.service.BlacklistService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/blacklist")
public class BlacklistController extends BaseController {

    private final BlacklistService blacklistService;

    public BlacklistController (BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping("/list")
    public BaseResponse findAllBlacklist(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findALLBlacklist u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<BlacklistEntity> response = blacklistService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllBlacklist u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, response.info());
        return response;
    }

    @PostMapping("/create")
    public BaseResponse createBlacklist(@RequestHeader Map<String, String> headers, @RequestBody CreateBlacklistRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createBlacklist u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = blacklistService.createBlacklist(request);
            }
        }
        logger.info("<=createBlacklist u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateBlacklist(@RequestHeader Map<String, String> headers, @RequestBody UpdateBlacklistRequest request) {

        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateBlacklist u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = blacklistService.updateBlacklist(request);
            }
        }
        logger.info("<=updateBlacklist u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteBlacklist(@RequestHeader Map<String, String> headers, @RequestBody DeleteBlacklistRequest request) {

        logger.info("=>deleteBlacklist req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = blacklistService.deleteBlacklist(request);
            }
        }
        logger.info("<=deleteBlacklist req: {}, resp: {}", request, response);
        return response;
    }
}
