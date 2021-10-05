package com.edso.resume.api.domain.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class RabbitMQAccess {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private int port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Bean("myRabbitMQ")
    public Connection getRabbitMQ() throws Exception {

        logger.info("RabbitMQ server: {}:{}", host, port);
        logger.info("RabbitMQ user: {}", username);
        logger.info("RabbitMQ pwd: {}", password);

        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(username);
        factory.setPassword(password);
        factory.setHost(host);
        factory.setPort(port);

        String url = "amqp://" + username + ":" + password + "@" + host + ":" + port;

        logger.info("rabbitmq info: {}", url);

        Connection connection = factory.newConnection();

        logger.info("Connect to RabbitMQ information: {}", url);

        return connection;
    }
}
