package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.request.CreateNoteProfileRequest;
import com.edso.resume.api.domain.request.DeleteNoteProfileRequest;
import com.edso.resume.api.domain.request.UpdateNoteProfileRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface NoteService {
    GetArrayResponse<NoteProfileEntity> findAllNote(HeaderInfo info, String idProfile, Integer page, Integer size);

    BaseResponse createNoteProfile(CreateNoteProfileRequest request);

    BaseResponse updateNoteProfile(UpdateNoteProfileRequest request);

    BaseResponse deleteNoteProfile(DeleteNoteProfileRequest request);
}
