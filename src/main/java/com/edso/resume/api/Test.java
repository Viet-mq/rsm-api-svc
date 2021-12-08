package com.edso.resume.api;

import com.edso.resume.api.domain.entities.TestEntity;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.MutablePropertyValues;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Test {
        public static void main(String[] args) throws Exception {
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
            System.out.println(toKhongDau("            Đào          Đình Dưong    "));
    }
//    public static void main(String[] args) {
////        String time = "11/01/1900";
////        Date da = new Date(time);
////        System.out.println(da.getTime());
//
//
////        Date date = new Date(-1100000000);
////
////        System.out.println(date);
////        String str = "qua            npham.docx";
////        System.out.println(str.length());
////        String[] array = str.split("\\.");
////        System.out.println(array[0]);
//
//        System.out.println(deAccent("Xin chào Việt Nam"));
//    }
//
//    public static String deAccent(String str) {
//        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
//        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").toLowerCase().trim();
//    }

    public static String toKhongDau(String str) {
        try {
            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").trim().toLowerCase().replaceAll("đ", "d");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


}
