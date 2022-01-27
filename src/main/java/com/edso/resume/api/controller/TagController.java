package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.TagEntity;
import com.edso.resume.api.domain.request.CreateTagRequest;
import com.edso.resume.api.domain.request.DeleteTagRequest;
import com.edso.resume.api.domain.request.UpdateTagRequest;
import com.edso.resume.api.service.TagService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tag")
public class TagController extends BaseController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public BaseResponse findAllTag(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllTag u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<TagEntity> resp = tagService.findAllTag(headerInfo, name, page, size);
        logger.info("<=findAllTag u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createTag(@RequestHeader Map<String, String> headers, @RequestBody CreateTagRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createTag u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = tagService.createTag(request);
            }
        }
        logger.info("<=createTag u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateTag(@RequestHeader Map<String, String> headers, @RequestBody UpdateTagRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateTag u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = tagService.updateTag(request);
            }
        }
        logger.info("<=updateTag u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteTag(@RequestHeader Map<String, String> headers, @RequestBody DeleteTagRequest request) {
        logger.info("=>deleteTag req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = tagService.deleteTag(request);
            }
        }
        logger.info("<=deleteTag req: {}, resp: {}", request, response);
        return response;
    }
}
