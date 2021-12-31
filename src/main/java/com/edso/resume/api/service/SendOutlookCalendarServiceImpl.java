package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.request.SendOutlookCalendarRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.http.HttpSender;
import com.edso.resume.lib.http.HttpSenderImpl;
import com.edso.resume.lib.response.BaseResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SendOutlookCalendarServiceImpl extends BaseService implements SendOutlookCalendarService {

    protected SendOutlookCalendarServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public BaseResponse sendOutlookCalendar(SendOutlookCalendarRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            HttpSender sender1 = new HttpSenderImpl();
            String url1 = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
            Map<String, String> headers1 = new HashMap<>();
            headers1.put("client_id", "0f4945ae-656d-4dc3-a1ed-b104e41610fc");
            headers1.put("scope", "user.read");
            headers1.put("refresh_token", "M.R3_BAY.-CZUf8ZamSjQF89JrrqmuKKS49XBh*1qznW9xW4d7G!HYElikdjHQWmk5M0x7uyhEXjP3qVND!50ccL1!vb3gSyzZV66ZhLFWpepSfhSJqYNcHsB1oTXm43*zsQW*nQxvzjZvUdO*ArQS2BbfKKXaXnQEV!QAub3j4v4bGkx7QPdQLhR*BEqkubMC8EHXzMBK9mqUvoYvnzqxwAHibxDClaVE7RWa5gcUhQBMU4bY6M5K4y5SQRt55ldHYWejS!4ZQfwMiQqDlPWm2m19HyW!2hHbnT9A8vS0xqsg!CMOqVkwLDtMyukxH*IUZodn9TOBaA$$");
            headers1.put("redirect_uri", "http://localhost");
            headers1.put("grant_type", "refresh_token");
            headers1.put("client_secret", "ogU7Q~AZ81lAtVSUCqYne4ggOEw6ph64yVOMA");
            JsonObject object1 = sender1.postForm(url1, null, headers1);

            String query1 = "{\n" +
                    "  \"subject\": \"" + request.getSubject() + "\",\n" +
                    "  \"body\": {\n" +
                    "    \"contentType\": \"HTML\",\n" +
                    "    \"content\": \"" + request.getContent() + "\"\n" +
                    "  },\n" +
                    "  \"start\": {\n" +
                    "      \"dateTime\": \"" + AppUtils.formatDateToString(new Date(request.getStartTime()), "yyyy-MM-dd'T'HH:mm:ss") + "\",\n" +
                    "      \"timeZone\": \"Asia/Bangkok\"\n" +
                    "  },\n" +
                    "  \"end\": {\n" +
                    "      \"dateTime\": \"" + AppUtils.formatDateToString(new Date(request.getEndTime()), "yyyy-MM-dd'T'HH:mm:ss") + "\",\n" +
                    "      \"timeZone\": \"Asia/Bangkok\"\n" +
                    "  },\n" +
                    "  \"location\":{\n" +
                    "      \"displayName\":\"" + request.getAddress() + "\"\n" +
                    "  },\n" +
                    "  \"attendees\": [\n" +

                    "    {\n" +
                    "      \"emailAddress\": {\n" +
                    "        \"address\":\"quanpk@edsolabs.com\",\n" +
                    "        \"name\": \"quanpham\"\n" +
                    "      },\n" +
                    "      \"type\": \"required\"\n" +
                    "    }\n" +

                    "  ]\n" +
                    "}";

            JsonObject jsonObject = new JsonParser().parse(query1).getAsJsonObject();

            HttpSender sender = new HttpSenderImpl();
            String url = "https://graph.microsoft.com/v1.0/me/events";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + object1.get("access_token").toString().replace("\"", ""));
            JsonObject object = sender.postJson(url, headers, jsonObject);

            baseResponse.setFailed(object.toString());
            return baseResponse;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            baseResponse.setFailed("Hệ thống bận");
            return baseResponse;
        }
    }
}
