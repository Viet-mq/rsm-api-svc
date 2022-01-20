package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.HistoryEmail;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.PresenterRequest;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.entities.HeaderInfo;

import java.util.List;
import java.util.UUID;

public class PublishPresenter extends PublishBase implements Runnable {
    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final PresenterRequest presenter;
    private final String profileId;
    private final String type;

    public PublishPresenter(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, PresenterRequest presenter, String profileId, String type) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.presenter = presenter;
        this.profileId = profileId;
        this.type = type;
    }

    @Override
    public void run() {
        String id = UUID.randomUUID().toString();
        HistoryEmail historyEmail = HistoryEmail.builder()
                .id(id)
                .idProfile(profileId)
                .subject(presenter.getSubjectPresenter())
                .content(presenter.getContentPresenter())
                .files(presenter.getFilePresenters())
                .build();

        List<String> listPath = historyEmailService.createHistoryEmail(historyEmail, info);
        rabbit.publishPresenterEmail(type, presenter, listPath, id, profileId);
    }
}
