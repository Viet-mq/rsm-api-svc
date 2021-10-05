package com.edso.resume.api.domain.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQOnlineSyncActions extends BaseRabbitAction {

    private final RabbitMQAccess rabbitMQAccess;

    public RabbitMQOnlineSyncActions(RabbitMQAccess rabbitMQAccess) {
        this.rabbitMQAccess = rabbitMQAccess;
    }

    public void publish(String queue, String message) {
        try {
            Connection connection = rabbitMQAccess.getRabbitMQ();
            Channel channel = connection.createChannel();
            channel.basicPublish("", queue, null, message.getBytes());
            logger.info("publish: {}", message);
        } catch (Exception e) {
            logger.info("Exception ", e);
        }
    }
}
