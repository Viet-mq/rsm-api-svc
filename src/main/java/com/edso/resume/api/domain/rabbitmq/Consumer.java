package com.edso.resume.api.domain.rabbitmq;

import com.edso.resume.api.domain.entities.UrlConsumerEntity;
import com.edso.resume.api.service.UrlConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    private final UrlConsumerService urlConsumerService;

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public Consumer(UrlConsumerService urlConsumerService) {
        this.urlConsumerService = urlConsumerService;
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void consumeUrl(UrlConsumerEntity url) {
        System.out.println("?????????????????????????");
        logger.info("Event from queue " + url);
        urlConsumerService.insertUrlToProfile(url);
    }
}
