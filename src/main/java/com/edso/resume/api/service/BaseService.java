package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public abstract class BaseService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final MongoDbOnlineSyncActions db;

    protected BaseService(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    protected Bson buildCondition(List<Bson> lst) {
        if (lst == null || lst.isEmpty()) {
            return new Document();
        }
        if (lst.size() == 1) {
            return lst.get(0);
        }
        return Filters.and(lst);
    }

    @SuppressWarnings(value = "unchecked")
    public List<String> parseList(Object list) {
        return (List<String>) list;
    }

    public String parseDate(Long time) {
        Date dateTime = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        return format.format(dateTime);
    }

    public String parseDateMonthYear(Long time) {
        Date dateTime = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(dateTime);
    }

    public String parseVietnameseToEnglish(String str) {
        try {
            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").trim().toLowerCase().replaceAll("đ", "d");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public String randomColor(){
        Random random = new Random();
        return String.format("#%06x", random.nextInt(256*256*256));
    }

}
