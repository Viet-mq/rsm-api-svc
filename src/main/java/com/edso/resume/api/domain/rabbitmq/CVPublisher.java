//package com.edso.resume.api.domain.rabbitmq;
//
//import com.rabbitmq.client.AMQP.BasicProperties;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import lombok.SneakyThrows;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.nio.charset.StandardCharsets;
//
//@RestController
//@RequestMapping("/event")
//public class CVPublisher {
//    private static final Logger logger = LoggerFactory.getLogger(CVPublisher.class);
//
//    @Value("${spring.rabbitmq.cv.queue}")
//    private String queue;
//
//    @Value("${spring.rabbitmq.cv.exchange}")
//    private String exchange;
//
//    @Value("${spring.rabbitmq.cv.routingkey}")
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
//    @SneakyThrows
//    @PostMapping("/publish")
//    public void publish(@RequestBody CV cv) {
////        CachingConnectionFactory factory = new CachingConnectionFactory(host);
////        factory.setUsername(username);
////        factory.setPassword(password);
////        BindingBuilder.bind(new Queue(queue, true))
////                .to(ExchangeBuilder.directExchange(exchange).durable(true).build())
////                .with(routingKey)
////                .noargs();
////        RabbitTemplate rabbit = new RabbitTemplate(factory);
////        rabbit.setMessageConverter(new Jackson2JsonMessageConverter());
////        rabbit.convertAndSend(exchange, routingKey, cv);
//
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(host);
//        factory.setPort(Integer.parseInt(port));
//        factory.setUsername(username);
//        factory.setPassword(password);
//        Connection connection = factory.newConnection();
//        Channel channel = connection.createChannel();
//        channel.queueDeclare(queue, true, false, false, null);
//        String msg = cv.toString();
//        BasicProperties messageProperties = new BasicProperties.Builder()
//                .contentType("application/json")
//                .build();
//        channel.basicPublish("", queue, messageProperties, msg.getBytes(StandardCharsets.UTF_8));
//        channel.close();
//        connection.close();
//
//        logger.info("=>published CV: {}", cv);
//    }
//
//
//}
