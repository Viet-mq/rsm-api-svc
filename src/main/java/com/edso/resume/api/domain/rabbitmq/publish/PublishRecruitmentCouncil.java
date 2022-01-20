package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.RecruitmentCouncilRequest;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.entities.HeaderInfo;

import java.util.List;
import java.util.UUID;

public class PublishRecruitmentCouncil implements Runnable {
    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final RecruitmentCouncilRequest recruitmentCouncil;
    private final String calendarId;
    private final String profileId;

    public PublishRecruitmentCouncil(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, RecruitmentCouncilRequest recruitmentCouncil, String calendarId, String profileId) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.recruitmentCouncil = recruitmentCouncil;
        this.calendarId = calendarId;
        this.profileId = profileId;
    }

    @Override
    public void run() {
        String id = UUID.randomUUID().toString();
        HistoryEmail historyEmail = HistoryEmail.builder()
                .id(id)
                .idProfile(profileId)
                .subject(recruitmentCouncil.getSubjectRecruitmentCouncil())
                .content(recruitmentCouncil.getContentRecruitmentCouncil())
                .files(recruitmentCouncil.getFileRecruitmentCouncils())
                .build();
        List<String> list = historyEmailService.createHistoryEmail(historyEmail, info);
        rabbit.publishRecruitmentCouncilEmail(TypeConfig.CALENDAR_INTERVIEWER, recruitmentCouncil, list, id, calendarId);
    }
}
