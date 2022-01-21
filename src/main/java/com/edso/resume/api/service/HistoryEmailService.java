package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEmailEntity;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HistoryEmailService {
    List<String> createHistoryEmail(String historyId, String profileId, String subject, String content, List<MultipartFile> files, HeaderInfo info);

    List<String> createHistoryEmails(List<IdEntity> ids, String subject, String content, List<MultipartFile> files, HeaderInfo info);

    void deleteHistoryEmail(String idProfile);

    GetArrayResponse<HistoryEmailEntity> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size);
}
