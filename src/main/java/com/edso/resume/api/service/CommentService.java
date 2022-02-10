package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CommentEntity;
import com.edso.resume.api.domain.request.CreateCommentRequest;
import com.edso.resume.api.domain.request.DeleteCommentRequest;
import com.edso.resume.api.domain.request.UpdateCommentRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface CommentService {
    GetArrayResponse<CommentEntity> findAllComment(HeaderInfo info, String idProfile, Integer page, Integer size);

    BaseResponse createCommentProfile(CreateCommentRequest request);

    BaseResponse updateCommentProfile(UpdateCommentRequest request);

    BaseResponse deleteCommentProfile(DeleteCommentRequest request);

    void deleteCommentProfileByIdProfile(String idProfile);
}
