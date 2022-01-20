package com.edso.resume.api.domain.rabbitmq.publish;

import com.edso.resume.api.domain.entities.HistoryEmails;
import com.edso.resume.api.domain.entities.IdEntity;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.api.domain.request.PresenterRequest;
import com.edso.resume.api.service.HistoryEmailService;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.entities.HeaderInfo;

import java.util.List;
import java.util.UUID;

public class PublishPresenters implements Runnable {
    private final RabbitMQOnlineActions rabbit;
    private final HistoryEmailService historyEmailService;
    private final HeaderInfo info;
    private final PresenterRequest presenter;
    private final List<IdEntity> ids;

    public PublishPresenters(RabbitMQOnlineActions rabbit, HistoryEmailService historyEmailService, HeaderInfo info, PresenterRequest presenter, List<IdEntity> ids) {
        this.rabbit = rabbit;
        this.historyEmailService = historyEmailService;
        this.info = info;
        this.presenter = presenter;
        this.ids = ids;
    }

    @Override
    public void run() {
        for (IdEntity idEntity : ids) {
            idEntity.setHistoryId(UUID.randomUUID().toString());
        }
        HistoryEmails historyEmails = HistoryEmails.builder()
                .ids(ids)
                .subject(presenter.getSubjectPresenter())
                .content(presenter.getContentPresenter())
                .files(presenter.getFilePresenters())
                .build();

        List<String> listPath = historyEmailService.createHistoryEmails(historyEmails, info);
        rabbit.publishPresenterEmails(TypeConfig.CALENDARS_PRESENTER, presenter, listPath, ids);
    }
}
