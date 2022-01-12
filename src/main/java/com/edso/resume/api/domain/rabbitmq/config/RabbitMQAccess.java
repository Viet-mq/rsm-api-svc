package com.edso.resume.api.domain.rabbitmq.config;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@Getter
@Setter
public class RabbitMQAccess {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Bean("myRabbitMQ")
    public Channel getChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        Connection connection = factory.newConnection();

        logger.info("RabbitMQ server: {}:{}", host, port);
        logger.info("RabbitMQ user: {}", username);
        logger.info("RabbitMQ pwd: {}", password);
        String uri = "amqp://" + username + ":" + password + "@" + host + ":" + port;
        logger.info("RabbitMQ info: {}", uri);
        logger.info("Connect to RabbitMQ information: {}", uri);

        return connection.createChannel();
    }
}
