package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.HistoryEmails;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.RecruitmentCouncilRequest;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.entities.HeaderInfo;

import java.util.List;
import java.util.UUID;

public class PublishRecruitmentCouncils implements Runnable {
    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final RecruitmentCouncilRequest recruitmentCouncil;
    private final List<IdEntity> ids;

    public PublishRecruitmentCouncils(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, RecruitmentCouncilRequest recruitmentCouncil, List<IdEntity> ids) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.recruitmentCouncil = recruitmentCouncil;
        this.ids = ids;
    }

    @Override
    public void run() {
        for (IdEntity idEntity : ids) {
            idEntity.setHistoryId(UUID.randomUUID().toString());
        }
        HistoryEmails historyEmails = HistoryEmails.builder()
                .ids(ids)
                .subject(recruitmentCouncil.getSubjectRecruitmentCouncil())
                .content(recruitmentCouncil.getContentRecruitmentCouncil())
                .files(recruitmentCouncil.getFileRecruitmentCouncils())
                .build();
        List<String> list = historyEmailService.createHistoryEmails(historyEmails, info);
        rabbit.publishRecruitmentCouncilEmails(TypeConfig.CALENDARS_INTERVIEWER, recruitmentCouncil, list, ids);
    }
}
