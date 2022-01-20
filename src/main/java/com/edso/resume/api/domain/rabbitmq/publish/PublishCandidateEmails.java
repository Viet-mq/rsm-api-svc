package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.HistoryEmails;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.CandidateRequest;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.entities.HeaderInfo;

import java.util.List;
import java.util.UUID;

public class PublishCandidateEmails implements Runnable {
    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final CandidateRequest candidate;
    private final List<IdEntity> ids;

    public PublishCandidateEmails(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, CandidateRequest candidate, List<IdEntity> ids) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.candidate = candidate;
        this.ids = ids;
    }

    @Override
    public void run() {
        for (IdEntity idEntity : ids) {
            idEntity.setHistoryId(UUID.randomUUID().toString());
        }
        HistoryEmails historyEmails = HistoryEmails.builder()
                .ids(ids)
                .subject(candidate.getSubjectCandidate())
                .content(candidate.getContentCandidate())
                .files(candidate.getFileCandidates())
                .build();
        List<String> list = historyEmailService.createHistoryEmails(historyEmails, info);
        rabbit.publishCandidateEmails(TypeConfig.CALENDARS_CANDIDATE, candidate, list, ids);

    }
}
