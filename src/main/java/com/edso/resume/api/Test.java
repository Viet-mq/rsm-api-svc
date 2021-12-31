package com.edso.resume.api;

//
//import com.edso.resume.api.domain.entities.TestEntity;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import com.rabbitmq.client.DeliverCallback;
//import org.springframework.beans.MutablePropertyValues;
//
//import java.text.Normalizer;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//public class Test {
//        public static void main(String[] args) throws Exception {
////        ConnectionFactory factory = new ConnectionFactory();
////        factory.setUsername("admin");
////        factory.setPassword("rabb@t@7911");
////        factory.setHost("18.139.222.137");
////        factory.setPort(5672);
////        Connection conn = factory.newConnection();
////
////        Channel channel = conn.createChannel();
////
////        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
////            String message3 = new String(delivery.getBody());
////            System.out.println(" [x] Received '" + message3 + "'");
////        };
////        channel.basicConsume("event.queue", true, deliverCallback, consumerTag -> {
////        });
//            System.out.println(toKhongDau("            Đào          Đình Dưong    "));
//    }
////    public static void main(String[] args) {
//////        String time = "11/01/1900";
//////        Date da = new Date(time);
//////        System.out.println(da.getTime());
////
////
//////        Date date = new Date(-1100000000);
//////
//////        System.out.println(date);
//////        String str = "qua            npham.docx";
//////        System.out.println(str.length());
//////        String[] array = str.split("\\.");
//////        System.out.println(array[0]);
////
////        System.out.println(deAccent("Xin chào Việt Nam"));
////    }
////
////    public static String deAccent(String str) {
////        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
////        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
////        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").toLowerCase().trim();
////    }
//
//    public static String toKhongDau(String str) {
//        try {
//            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
//            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//            return pattern.matcher(temp).replaceAll("").trim().toLowerCase().replaceAll("đ", "d");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }
//
//
//}

import com.edso.resume.lib.http.HttpSender;
import com.edso.resume.lib.http.HttpSenderImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.util.Assert;
import springfox.documentation.spring.web.json.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) throws IOException {


        String query1 = "{\n" +
                "  \"subject\": \"Let's go for lunch...\"\n" +
                "}";
//        String query = "{\n" +
//                "  \"name\": \"Let's go for lunch....\",\n" +
//                "  \"officeName\": \"Let's go for lunch....\"\n" +
//                "}";

        JsonObject jsonObject = new JsonParser().parse(query1).getAsJsonObject();

//        Long a = System.currentTimeMillis();
//        HttpSender sender1 = new HttpSenderImpl();
//        String url1 = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
//        Map<String, String> headers1 = new HashMap<>();
//        headers1.put("client_id", "0f4945ae-656d-4dc3-a1ed-b104e41610fc");
//        headers1.put("scope", "user.read");
//        headers1.put("refresh_token", "M.R3_BAY.-CZUf8ZamSjQF89JrrqmuKKS49XBh*1qznW9xW4d7G!HYElikdjHQWmk5M0x7uyhEXjP3qVND!50ccL1!vb3gSyzZV66ZhLFWpepSfhSJqYNcHsB1oTXm43*zsQW*nQxvzjZvUdO*ArQS2BbfKKXaXnQEV!QAub3j4v4bGkx7QPdQLhR*BEqkubMC8EHXzMBK9mqUvoYvnzqxwAHibxDClaVE7RWa5gcUhQBMU4bY6M5K4y5SQRt55ldHYWejS!4ZQfwMiQqDlPWm2m19HyW!2hHbnT9A8vS0xqsg!CMOqVkwLDtMyukxH*IUZodn9TOBaA$$");
//        headers1.put("redirect_uri", "http://localhost");
//        headers1.put("grant_type", "refresh_token");
//        headers1.put("client_secret", "ogU7Q~AZ81lAtVSUCqYne4ggOEw6ph64yVOMA");
//        JsonObject object = sender1.
//        System.out.println(object);
//        System.out.println(System.currentTimeMillis() - a);



        HttpSender sender = new HttpSenderImpl();
        String url = "https://graph.microsoft.com/v1.0/me/events";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        headers.put("Authorization", "Bearer EwB4A8l6BAAUwihrrCrmQ4wuIJX5mbj7rQla6TUAAef7Ef4smX44iofB3/uZIVTVNd/vZ3FF8dOaF4JGsw22ZKTSdRMNB9JAI6NmfDb0kZdlfrBC1HgguAjHcNbKcKc0OkOMxuHkN1ffIvoY5dPQKRa9LdozVs9J6acX4UY9O7mKzbMNu2WdjRf2NiitxYv/ODWhDBppkYe/FI3QCaKnllPE0F3zZl74gWIjCNMefFhG0QLbDvIbme0bIytjU/Y63oynEUh80VutIUEcc1V1j2PLTUQKZ0jPV3WmTWHLGLy8+RXr/k5CX71V6nXMe7mb1HteV2ZTdNswWrKbKVQIMtLrtP3U5hb+P0GVqhJVVbRXsbsRS3ZLNgmCIORChQEDZgAACFPclydLuFpLSAIbTvBgkT9v7X4s1hHYhPbBce3kUwGTsFVBRTyiBdyUoyFtsiRpPx3ReM4fLDe6aN6mejXUQGIIOBiXb0ADghmzqWgXTk3wzo02go2RWlyiByKiri/u4QpKiCPQ38JjJCsEzgwAgdZ/UNE6fkbMutxcBz/5mapBnVvF51qhDTtnCbgyG1N407prUpkeG+YgnZztjoT3eUmV1jwE36Eks1FP/cZYvPcyoO4mWLrhEh1Z7QUzHUhQ71OIH+xTNgC8yHVJ3xfzD8biOF2GQBmSyWcMRou5RUbhM/6Uy6vMQFE+oTUdGLuJywIsOyzjd5H/dNAE/dTUpHovXHebNrImBwBUMj8lmjsLnt+k9rcrpnO03AcKSWUDWJmNSR6ZvC2xyDymUqPDKMnNZH91RQdyz8GN0+eOmXZ0ZijOAyToff6qkhovnyrc1JmJ1I0eFsGQaSH6MO7vqk0muYscRL8lzHIykYPq/PwtHTP19b7o4bSc3nUNM4VLtJ3ndYSOO4b3glEKVZIHXiAq19rrrdksp6xQs6X0ZkdTbkJa7Gy2QzJGpOTwXs3ZrQ2JzBEU7IifPG9l6DEb0XVHvQfyTV6bYIy660AELU6nZByzkwIfirVYVb+ZhyoPn4abvzIK7VxTm9rB4qLvGtkdxA80S9IBLHItsTFL/rQF662S5RwhzMRIfRK0eqLRUnLPLa/zYT+zOGQUgPOaxSz6pyhp71BuLqag58baZZ/mo555hhP8moZhkKwc3E7JSDUEuAfUeamHW3sdkP8tkI8UZ4MC");
        JsonObject object = sender.postJson(url, headers, jsonObject);
        System.out.println(object);


//        URL url = new URL("https://graph.microsoft.com/v1.0/me/events");
//        HttpURLConnection con1 = (HttpURLConnection) url.openConnection();
//        con1.setRequestMethod("GET");
//        con1.setRequestProperty("Content-Type", "application/json");
//        con1.setRequestProperty("Authorization", "Bearer EwB4A8l6BAAUwihrrCrmQ4wuIJX5mbj7rQla6TUAAdPFM7untrThR9c1BMHFCKlfrG7Xo0AJuFhuROXaqeDhCI5clVQ8k5RDvafVyCWOmtoB6716GLxW384B5AvGgUhMLaNmpeJCWbixRS8daUw/oAorUFP0IsozPX1EZvZObDJWZB0kVQEiFLvlrH0/vFqOK58wgQvGPgp3asZ+/i5UR1gIg/1XdA/ERTmfyZr60zuDXqBg0Y53ZiFnIRFHGdvorJsnHNC+3vAMiSuYSLg1C5NdLxzEGjBpkjlT8Y6+fRyQpEnauo/3iNSga7rDgmk8IlI2YiDmGYPbsBKT7qqxpFP/xkeCVskgmva4Y9AVRP/qMU352UF17dnbVuUKzioDZgAACH1gMiRQogicSALrd6AA1bDCcOdxSUBliHvSQnJ1Z7vscuqRyu4tASJFJPcn/TNBBefHA94qSS5XW4t9moEXb4iX5zkmIY7Hm6DeIlYNlqxF+opl6b+ci3nbRPtunjCg+CmiO5bS2TD2IT4Tixn0ilCfBNH5Y+JA1C9eh4l5cm9FN0Sx+ckOUuzPC9rSzhrbj69HzjrrkcXUJwzzbnp3YyCXmeio2pK/Uu+uvuZhPoCUh7+JVsb7wwSaKIUlJcDrn3ea7FzFJSuq1APwT7tchyUCCk1yfYiJqFVBXSfcpg/ZTKdudzIgobz8S5t8EM1I1kcIkUWMgBXz2NLziQBlhnFO0YigFMozvZvVPz8yRpyeKFXjJM9A9GPd4JjaF1HUuTCVFb3ksxRtduzB7x+oNzSn/E2IdnML2Y6GpsWmPmQy59VITrTNG3QPnok+FLZb8aqOpEz9Yu0ng49wXRUd/xXKEUWMQ56P1MOk5nHZMQZ3uYWl7j/XFc75kPMEbvfNBPnOE8B5AbWf3jDx0e3Ui+UDE6n95DhWZ4ZRuNRg5WrCa0U46imnFBs0Xl2ikcR9XESfD7NgwQ72irNPMQGnsUVQxV6v9jVHSUUKE8tR+Q4Om9Af7PLu7UWvxIg80oPz7gNQS0JmUrMkpoEJHVjmfFa6zrlSsU40+R8cdlL/2OIJmmKTyQvEKdtyrynFigOIbqiWVh+y4GAxXuEkgm6rQqWmQBvghRuWJhrmbckYV38p0stOAlxGjgYF4ZDjFR9Qjn2R9HFRoy6ZmrfN9mapPVxXmIMC");
//
//        String query1 = "{\n" +
//                "  \"subject\": \"Let's go for lunch...\"\n" +
//                "}";
//
//        // For POST only - START
//        con1.setDoOutput(true);
//        OutputStream os = con1.getOutputStream();
//        os.write(query1.getBytes());
//        os.flush();
//        os.close();
//        // For POST only - END
//        String[] resp = new String[0];
//        BufferedReader in = new BufferedReader(new InputStreamReader(
//                con1.getInputStream()));
//        String inputLine;
//        StringBuilder response = new StringBuilder();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//
//        // print result
//        System.out.println(response);


//        Long a = System.currentTimeMillis();
//        String query = "client_id=" + URLEncoder.encode("0f4945ae-656d-4dc3-a1ed-b104e41610fc", "UTF-8");
//        query += "&";
//        query += "scope=user.read";
//        query += "&";
//        query += "refresh_token=" + URLEncoder.encode("M.R3_BAY.-CZUf8ZamSjQF89JrrqmuKKS49XBh*1qznW9xW4d7G!HYElikdjHQWmk5M0x7uyhEXjP3qVND!50ccL1!vb3gSyzZV66ZhLFWpepSfhSJqYNcHsB1oTXm43*zsQW*nQxvzjZvUdO*ArQS2BbfKKXaXnQEV!QAub3j4v4bGkx7QPdQLhR*BEqkubMC8EHXzMBK9mqUvoYvnzqxwAHibxDClaVE7RWa5gcUhQBMU4bY6M5K4y5SQRt55ldHYWejS!4ZQfwMiQqDlPWm2m19HyW!2hHbnT9A8vS0xqsg!CMOqVkwLDtMyukxH*IUZodn9TOBaA$$", "UTF-8");
//        query += "&";
//        query += "redirect_uri=http://localhost";
//        query += "&";
//        query += "grant_type=refresh_token";
//        query += "&";
//        query += "client_secret=" + URLEncoder.encode("ogU7Q~AZ81lAtVSUCqYne4ggOEw6ph64yVOMA", "UTF-8");
//
//        URL myurl = new URL("https://login.microsoftonline.com/common/oauth2/v2.0/token");
//        HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
//        con.setRequestMethod("POST");
//
//        // For POST only - START
//        con.setDoOutput(true);
//        OutputStream os = con.getOutputStream();
//        os.write(query.getBytes());
//        os.flush();
//        os.close();
//        // For POST only - END
//        String[] resp = new String[0];
//        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    con.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            // print result
//            resp = response.toString().split("\"");
//            System.out.println(resp[15]);
//        } else {
//            System.out.println("POST request not worked");
//        }

    }

}


