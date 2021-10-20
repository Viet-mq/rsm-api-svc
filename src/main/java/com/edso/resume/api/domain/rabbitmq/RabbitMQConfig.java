//package com.edso.resume.api.domain.rabbitmq;
//
//import com.edso.resume.api.domain.db.BaseAction;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig extends BaseAction {
//
//    @Value("${spring.rabbitmq.profile.queue}")
//    private String queue;
//
//    @Value("${spring.rabbitmq.profile.exchange}")
//    private String exchange;
//
//    @Value("${spring.rabbitmq.profile.routingkey}")
//    private String routingKey;
//
//    @Value("${spring.rabbitmq.username}")
//    private String username;
//
//    @Value("${spring.rabbitmq.password}")
//    private String password;
//
//    @Value("${spring.rabbitmq.host}")
//    private String host;
//
//    @Value("${spring.rabbitmq.port}")
//    private String port;
//
//    @Bean
//    Queue queue() {
//        return new Queue(queue, true);
//    }
//
//    @Bean
//    Exchange myExchange() {
//        return ExchangeBuilder.directExchange(exchange).durable(true).build();
//    }
//
//    @Bean
//    Binding binding() {
//        return BindingBuilder
//                .bind(queue())
//                .to(myExchange())
//                .with(routingKey)
//                .noargs();
//    }
//
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
//        cachingConnectionFactory.setUsername(username);
//        cachingConnectionFactory.setPassword(password);
//        return cachingConnectionFactory;
//    }
//
//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//
//        logger.info("RabbitMQ server: {}:{}", host, port);
//        logger.info("RabbitMQ user: {}", username);
//        logger.info("RabbitMQ pwd: {}", password);
//        String uri = "amqp://" + username + ":" + password + "@" + host + ":" + port;
//        logger.info("RabbitMQ info: {}", uri);
//        logger.info("Connect to RabbitMQ information: {}", uri);
//
//        rabbitTemplate.setMessageConverter(jsonMessageConverter());
//        return rabbitTemplate;
//    }
//}