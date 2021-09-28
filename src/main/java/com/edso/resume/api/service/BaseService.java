package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public abstract class BaseService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Bson buildCondition(List<Bson> lst) {
        if (lst == null || lst.isEmpty()) {
            return new Document();
        }
        if (lst.size() == 1) {
            return lst.get(0);
        }
        return Filters.and(lst);
    }

    public void createHistory(String idProfile, String action, String by, MongoDbOnlineSyncActions db)  {

        Document history = new Document();
        history.append("id", UUID.randomUUID().toString());
        history.append("idProfile", idProfile);
        history.append("time", System.currentTimeMillis());
        history.append("action", action);
        history.append("by", by);

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_HISTORY_PROFILE, history);
    }

    public String parseDate(Long milliSeconds){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
