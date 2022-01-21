package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.HistoryEmailEntity;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/history-email")
public class HistoryEmailController extends BaseController {
    private final HistoryEmailService historyEmailService;

    public HistoryEmailController(HistoryEmailService historyEmailService) {
        this.historyEmailService = historyEmailService;
    }

    @GetMapping("/list")
    public BaseResponse findAllHistoryEmailProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile") String idProfile,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllHistoryEmailProfile u: {}, idProfile: {}, page: {}, size: {}", headerInfo, idProfile, page, size);
        GetArrayResponse<HistoryEmailEntity> resp = historyEmailService.findAllHistoryEmail(headerInfo, idProfile, page, size);
        logger.info("<=findAllHistoryEmailProfile u: {}, idProfile: {}, page: {}, size: {}, resp: {}", headerInfo, idProfile, page, size, resp.info());
        return resp;
    }
}
