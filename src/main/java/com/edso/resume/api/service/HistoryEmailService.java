package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.HistoryEmailEntity;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface HistoryEmailService {
    void createHistoryEmail(String type, String profileId, List<String> usernames, String email, String subject, String content, List<MultipartFile> files, HeaderInfo info) throws IOException, TimeoutException;

    void createHistoryEmails(String type, List<IdEntity> ids, List<String> usernames, String email, String subject, String content, List<MultipartFile> files, HeaderInfo info) throws IOException, TimeoutException;

    void deleteHistoryEmail(String idProfile);

    GetArrayResponse<HistoryEmailEntity> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size);
}
