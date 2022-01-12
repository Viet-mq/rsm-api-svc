package com.edso.resume.api.domain.rabbitmq;

import com.edso.resume.api.domain.rabbitmq.config.RabbitMQAccess;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
public class CVPublisher {
    private static final Logger logger = LoggerFactory.getLogger(CVPublisher.class);

    private final RabbitMQAccess rabbit;

    @Value("${spring.rabbitmq.cv.queue}")
    private String queue;

    @Value("${spring.rabbitmq.cv.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.cv.routingkey}")
    private String routingkey;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private String port;

    public CVPublisher(RabbitMQAccess rabbit) {
        this.rabbit = rabbit;
    }

    @SneakyThrows
    @PostMapping("/publish")
    public void publish(@RequestBody CV cv) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(Integer.parseInt(port));
        factory.setUsername(username);
        factory.setPassword(password);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue, true, false, false, null);
        BasicProperties messageProperties = new BasicProperties.Builder()
                .contentType("application/json")
                .build();
        channel.basicPublish("", queue, messageProperties, new Gson().toJson(cv).getBytes());
        channel.close();
        connection.close();

        logger.info("=>published CV: {}", cv);
    }


}
