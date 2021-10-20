package com.edso.resume.api.domain.rabbitmq;

import com.edso.resume.api.domain.db.BaseAction;
import com.edso.resume.api.domain.entities.EmailMessageEntity;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMQOnlineActions extends BaseAction {

    private final RabbitMQAccess rabbitMQAccess;
    @Value("${spring.rabbitmq.email.queue}")
    private String queue;

    @Value("${spring.rabbitmq.email.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.email.routingkey}")
    private String routingKey;

    public RabbitMQOnlineActions(RabbitMQAccess rabbitMQAccess) {
        this.rabbitMQAccess = rabbitMQAccess;
    }

    public void insertEmailToRabbit(String email, String time) throws IOException, TimeoutException {
        Connection connection = rabbitMQAccess.getConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, routingKey);
        EmailMessageEntity messageEntity = new EmailMessageEntity();
        messageEntity.setToEmail(email);
        messageEntity.setMessage("Bạn có cuộc phỏng tại công ty Edsolabs\n" +
                "Tại: No 9, Lane 4, Duy Tân, Dịch Vọng Hậu, Cầu Giấy, Hà Nội\n" +
                "Thời gian: " + time);
        BasicProperties messageProperties = new BasicProperties.Builder()
                .contentType("application/json")
                .build();
        channel.basicPublish(exchange, routingKey, messageProperties, messageEntity.toString().getBytes());
    }

}
