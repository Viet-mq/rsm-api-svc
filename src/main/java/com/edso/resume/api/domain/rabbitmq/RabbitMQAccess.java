package com.edso.resume.api.domain.rabbitmq;


import com.edso.resume.api.domain.db.BaseAction;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMQAccess extends BaseAction {
    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    public Connection getConnection() throws IOException, TimeoutException {
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

        return connection;
    }
}
