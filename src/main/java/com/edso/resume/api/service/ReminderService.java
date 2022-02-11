package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReminderEntity;
import com.edso.resume.api.domain.request.CreateReminderRequest;
import com.edso.resume.api.domain.request.DeleteReminderRequest;
import com.edso.resume.api.domain.request.UpdateReminderRequest;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;

public interface ReminderService {
    GetArrayResponse<ReminderEntity> findAll(HeaderInfo info, Long from, Long to);

    BaseResponse createReminder(CreateReminderRequest request);

    BaseResponse updateReminder(UpdateReminderRequest request);

    BaseResponse deleteReminder(DeleteReminderRequest request);
}
