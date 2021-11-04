package com.edso.resume.api;

public class Test {
    //    public static void main(String[] args) throws Exception {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setUsername("admin");
//        factory.setPassword("rabb@t@7911");
//        factory.setHost("18.139.222.137");
//        factory.setPort(5672);
//        Connection conn = factory.newConnection();
//
//        Channel channel = conn.createChannel();
//
//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message3 = new String(delivery.getBody());
//            System.out.println(" [x] Received '" + message3 + "'");
//        };
//        channel.basicConsume("event.queue", true, deliverCallback, consumerTag -> {
//        });
//
//    }
    public static void main(String[] args) {
        String str = "quanpham.docx";
        String[] array = str.split("\\.");
        System.out.println(array[0]);
    }
}
