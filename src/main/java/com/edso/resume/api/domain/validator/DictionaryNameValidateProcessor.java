package com.edso.resume.api.domain.validator;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ThreadConfig;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryNameValidateProcessor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DictionaryNameValidatorResult result;
    private final MongoDbOnlineSyncActions db;
    private final IDictionaryNameValidator target;
    private final String key;
    private final String type;
    private final String name;

    public DictionaryNameValidateProcessor(String key, String type, String name, MongoDbOnlineSyncActions db, IDictionaryNameValidator target) {
        this.key = key;
        this.type = type;
        this.name = name;
        this.db = db;
        this.target = target;
        this.result = new DictionaryNameValidatorResult(type);
    }

    @Override
    public void run() {
        try {
            Bson cond;
            switch (type) {
                case ThreadConfig.BLACKLIST_EMAIL: {
                    cond = Filters.eq(DbKeyConfig.EMAIL, this.name);
                    break;
                }
                case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                    cond = Filters.eq(DbKeyConfig.PHONE_NUMBER, this.name);
                    break;
                }
                default: {
                    cond = Filters.eq(DbKeyConfig.NAME, this.name);
                    break;
                }
            }
            Document doc = db.findOne(getCollectionName(), cond);
            if (doc == null) {
                if (type.equals(ThreadConfig.BLACKLIST_EMAIL) || type.equals(ThreadConfig.BLACKLIST_PHONE_NUMBER)) {
                    result.setResult(true);
                    return;
                }
                result.setResult(false);
                return;
            }
            if (type.equals(ThreadConfig.BLACKLIST_EMAIL) || type.equals(ThreadConfig.BLACKLIST_PHONE_NUMBER)) {
                result.setResult(false);
            } else {
                result.setResult(true);
                result.setId(AppUtils.parseString(doc.get(DbKeyConfig.ID)));
            }
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            result.setResult(false);
            result.setId("Hệ thống đang bận");
        } finally {
            target.onValidatorResult(this.key, result);
        }
    }

    public DictionaryNameValidatorResult getResult() {
        return result;
    }

    private String getCollectionName() {
        switch (type) {
            case ThreadConfig.JOB: {
                return CollectionNameDefs.COLL_JOB;
            }
            case ThreadConfig.JOB_LEVEL: {
                return CollectionNameDefs.COLL_JOB_LEVEL;
            }
            case ThreadConfig.SCHOOL: {
                return CollectionNameDefs.COLL_SCHOOL;
            }
            case ThreadConfig.SOURCE_CV: {
                return CollectionNameDefs.COLL_SOURCE_CV;
            }
            case ThreadConfig.PROFILE: {
                return CollectionNameDefs.COLL_PROFILE;
            }
            case ThreadConfig.STATUS_CV: {
                return CollectionNameDefs.COLL_STATUS_CV;
            }
            case ThreadConfig.DEPARTMENT: {
                return CollectionNameDefs.COLL_DEPARTMENT;
            }
            case ThreadConfig.CALENDAR: {
                return CollectionNameDefs.COLL_CALENDAR_PROFILE;
            }
            case ThreadConfig.BLACKLIST_EMAIL:
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                return CollectionNameDefs.COLL_BLACKLIST;
            }
            default: {
                return null;
            }
        }
    }
}
