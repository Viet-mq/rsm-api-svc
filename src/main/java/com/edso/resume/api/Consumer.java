package com.edso.resume.api;

import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private final static String QUEUE_NAME = "email.queue";

    public static void main(String[] argv) throws Exception {
        System.out.println("Create a ConnectionFactory");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("18.139.222.137");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("rabb@t@7911");

        System.out.println("Create a Connection");
        System.out.println("Create a Channel");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        System.out.println("Create a queue " + QUEUE_NAME);
//        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        System.out.println("Start receiving messages ... ");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received: '" + message + "'");
        };
        CancelCallback cancelCallback = consumerTag -> {
        };
        String consumerTag = channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);
        System.out.println("consumerTag: " + consumerTag);
    }
}