package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.CandidateRequest;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.entities.HeaderInfo;

import java.util.List;
import java.util.UUID;

public class PublishCandidateEmail implements Runnable {

    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final CandidateRequest candidate;
    private final String profileId;
    private final String type;

    public PublishCandidateEmail(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, CandidateRequest candidate, String profileId, String type) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.candidate = candidate;
        this.profileId = profileId;
        this.type = type;
    }

    @Override
    public void run() {
        String id = UUID.randomUUID().toString();
        HistoryEmail historyEmail = HistoryEmail.builder()
                .id(id)
                .idProfile(profileId)
                .subject(candidate.getSubjectCandidate())
                .content(candidate.getContentCandidate())
                .files(candidate.getFileCandidates())
                .build();
        List<String> list = historyEmailService.createHistoryEmail(historyEmail, info);
        rabbit.publishCandidateEmail(type, candidate, list, id, profileId);
    }
}
