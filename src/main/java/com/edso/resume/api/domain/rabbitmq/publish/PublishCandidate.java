package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.Candidate;
import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.google.common.base.Strings;

import java.util.UUID;

public class PublishCandidate implements Runnable {

    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final Candidate candidate;

    public PublishCandidate(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, Candidate candidate) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.candidate = candidate;
    }

    @Override
    public void run() {
        if (Strings.isNullOrEmpty(candidate.getEmail()) || Strings.isNullOrEmpty(candidate.getContent()) || Strings.isNullOrEmpty(candidate.getSubject())) {
            return;
        }
        String id = UUID.randomUUID().toString();
        HistoryEmail historyEmail = HistoryEmail.builder()
                .id(id)
                .idProfile(candidate.getIdProfile())
                .subject(candidate.getSubject())
                .content(candidate.getContent())
                .files(candidate.getFiles())
                .build();
        historyEmailService.createHistoryEmail(historyEmail, info);
        candidate.setId(id);
        rabbit.publishEmail(TypeConfig.CALENDAR_CANDIDATE, candidate);
    }
}
