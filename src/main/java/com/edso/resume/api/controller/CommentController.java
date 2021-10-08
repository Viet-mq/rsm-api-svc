package com.edso.resume.api.controller;

import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController extends BaseController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/create")
    public BaseResponse createComment(@RequestHeader Map<String, String> headers, @RequestBody CreateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createComment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setFailed("Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = commentService.createComment(request);
            }
        }
        logger.info("<=createComment u: {}, req: {}, rep: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateComment(@RequestHeader Map<String, String> headers, @RequestBody UpdateCommentRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateComment u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setFailed("Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = commentService.updateComment(request);
            }
        }
        logger.info("<= updateComment u: {}, req: {}, rep: {}", headerInfo, request, response);
        return response;
    }
}
