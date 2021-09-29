package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.request.CreateHistoryRequest;
import com.edso.resume.api.service.HistoryService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController extends BaseController{

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService){
        this.historyService = historyService;
    }

    @GetMapping("/list")
    public BaseResponse findAllHistoryProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile") String idProfile,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllHistoryProfile u: {}, idProfile: {}, page: {}, size: {}", headerInfo, idProfile, page, size);
        GetArrayResponse<HistoryEntity> resp = historyService.findAllHistory(headerInfo, idProfile, page, size);
        logger.info("<=findAllHistoryProfile u: {}, idProfile: {}, page: {}, size: {}, resp: {}", headerInfo, idProfile, page, size, resp.info());
        return resp;
    }

}
