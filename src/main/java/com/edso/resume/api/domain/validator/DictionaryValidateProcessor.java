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

public class DictionaryValidateProcessor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DictionaryValidatorResult result;
    private final MongoDbOnlineSyncActions db;
    private final IDictionaryValidator target;
    private final String key;
    private final String type;
    private final String id;

    public DictionaryValidateProcessor(String key, String type, String id, MongoDbOnlineSyncActions db, IDictionaryValidator target) {
        this.key = key;
        this.type = type;
        this.id = id;
        this.db = db;
        this.target = target;
        this.result = new DictionaryValidatorResult(type);
    }

    @Override
    public void run() {
        try {
            Bson cond = Filters.eq(DbKeyConfig.ID, this.id);
            Document doc = db.findOne(getCollectionName(), cond);
            if (doc == null) {
                result.setResult(false);
                result.setName("Không tồn tại " + getDictionaryName() + " này!");
                return;
            }
            result.setResult(true);
            if (type.equals(ThreadConfig.PROFILE)) {
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)));
            } else {
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.NAME)));
            }
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            result.setResult(false);
            result.setName("Hệ thống đang bận");
        } finally {
            target.onValidatorResult(this.key, result);
        }
    }

    public DictionaryValidatorResult getResult() {
        return result;
    }

    private String getDictionaryName() {
        switch (type) {
            case ThreadConfig.JOB: {
                return "công việc";
            }
            case ThreadConfig.JOB_LEVEL: {
                return "vị trí tuyển dụng";
            }
            case ThreadConfig.SCHOOL: {
                return "trường học";
            }
            case ThreadConfig.SOURCE_CV: {
                return "nguồn cv";
            }
            case ThreadConfig.PROFILE: {
                return "id profile";
            }
            case ThreadConfig.STATUS_CV: {
                return "trạng thái cv";
            }
            case ThreadConfig.DEPARTMENT: {
                return "phòng ban";
            }
            case ThreadConfig.CALENDAR: {
                return "id calendar";
            }
            default: {
                return null;
            }
        }
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
            default: {
                return null;
            }
        }
    }

}
