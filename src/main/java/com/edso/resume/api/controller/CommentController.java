package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CommentEntity;
import com.edso.resume.api.domain.request.CreateCommentRequest;
import com.edso.resume.api.domain.request.DeleteCommentRequest;
import com.edso.resume.api.domain.request.UpdateCommentRequest;
import com.edso.resume.api.service.CommentService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/commnet")
public class CommentController extends BaseController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/list")
    public BaseResponse findAllComment(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllComment u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<CommentEntity> resp = commentService.findAllComment(headerInfo, name, page, size);
        logger.info("<=findAllComment u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createCommentProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createCommentProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = commentService.createCommentProfile(request);
            }
        }
        logger.info("<=createCommentProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateCommentProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateCommentProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = commentService.updateCommentProfile(request);
            }
        }
        logger.info("<=updateCommentProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteCommentProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteCommentRequest request) {
        logger.info("=>deleteCommentProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = commentService.deleteCommentProfile(request);
            }
        }
        logger.info("<=deleteCommentProfile req: {}, resp: {}", request, response);
        return response;
    }
}
