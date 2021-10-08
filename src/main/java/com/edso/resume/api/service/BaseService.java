package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MongoDbOnlineSyncActions db;
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

    public boolean validateDictionary (String id, String coll){
        Bson con = Filters.eq("id", id);
        Document idDocument = db.findOne(coll, con);
        if (idDocument == null) {
            return false;
        }
        return true;
    }

}
