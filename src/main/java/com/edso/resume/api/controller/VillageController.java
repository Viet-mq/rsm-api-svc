package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.VillageEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.VillageService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/village")
public class VillageController extends BaseController {

    private final VillageService villageService;

    public VillageController(VillageService villageService) {
        this.villageService = villageService;
    }

    @GetMapping("/list")
    public BaseResponse findAllVillage(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllVillage u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<VillageEntity> resp = villageService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllVillage u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createVillage(@RequestHeader Map<String, String> headers, @RequestBody CreateVillageRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createVillage u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = villageService.createVillage(request);
            }
        }
        logger.info("<=createVillage u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateVillage(@RequestHeader Map<String, String> headers, @RequestBody UpdateVillageRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateVillage u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = villageService.updateVillage(request);
            }
        }
        logger.info("<=updateVillage u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteVillage(@RequestHeader Map<String, String> headers, @RequestBody DeleteVillageRequest request) {
        logger.info("=>deleteVillage req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = villageService.deleteVillage(request);
            }
        }
        logger.info("<=deleteVillage req: {}, resp: {}", request, response);
        return response;
    }

}
