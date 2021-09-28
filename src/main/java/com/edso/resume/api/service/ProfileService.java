package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarReponse;
import com.edso.resume.lib.response.GetArrayResponse;

import java.io.IOException;

public interface ProfileService {

    GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, String idProfile, Integer page, Integer size);

    GetArrayCalendarReponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile);

    GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size);

    GetArrayResponse<NoteProfileEntity> findAllNote(HeaderInfo info, String idProfile, Integer page, Integer size);

    BaseResponse createProfile(CreateProfileRequest request);

    BaseResponse updateProfile(UpdateProfileRequest request);

    BaseResponse deleteProfile(DeleteProfileRequest request);

    BaseResponse updateStatusProfile(UpdateStatusProfileRequest request);

    byte[] exportExcel(HeaderInfo info, String fullName) throws IOException;

    BaseResponse createNoteProfile(CreateNoteProfileRequest request);

    BaseResponse createCalendarProfile(CreateCalendarProfileRequest request);

    BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest request);

    BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request);
}
