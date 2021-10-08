package com.edso.resume.api.service;

import com.edso.resume.api.domain.request.CreateCommentRequest;
import com.edso.resume.api.domain.request.UpdateCommentRequest;
import com.edso.resume.lib.response.BaseResponse;

public interface CommentService {
    BaseResponse createComment(CreateCommentRequest request);

    BaseResponse updateComment(UpdateCommentRequest request);
}
