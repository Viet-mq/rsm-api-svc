package com.edso.resume.api.service;

import com.edso.resume.api.domain.Object.Comment;
import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    @SuppressWarnings (value="unchecked")
    public List<String> parseList(Object list){
        return (List<String>) list;
    }

    @SuppressWarnings (value="unchecked")
    public List<Comment> parseListComment(Object list){
        return (List<Comment>) list;
    }

}
