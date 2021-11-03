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
            Bson cond = getCondition();
            Document doc = db.findOne(getCollectionName(), cond);
            if (doc == null) {
                if (type.equals(ThreadConfig.BLACKLIST_EMAIL) || type.equals(ThreadConfig.BLACKLIST_PHONE_NUMBER) || type.equals(ThreadConfig.PROFILE_EMAIL )|| type.equals(ThreadConfig.PROFILE_PHONE_NUMBER)) {
                    result.setResult(true);
                    return;
                }
                result.setResult(false);
                result.setName("Không tồn tại " + getDictionaryName() + " này!");
                return;
            }
            setName(doc);
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

    private void setName(Document doc){
        switch (type) {
            case ThreadConfig.PROFILE: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)));
                break;
            }
            case ThreadConfig.USER: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)));
                break;
            }
            case ThreadConfig.NOTE: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.PATH_FILE)));
                result.setIdProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)));
                break;
            }
            case ThreadConfig.CALENDAR: {
                result.setResult(true);
                result.setIdProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)));
                break;
            }
            case ThreadConfig.BLACKLIST_EMAIL:
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                result.setResult(false);
                result.setName("Ứng viên này đang trong blacklist!");
                break;
            }
            case ThreadConfig.PROFILE_EMAIL:{
                result.setResult(false);
                result.setName("Đã tồn tại ứng viên có email này");
                break;
            }
            case ThreadConfig.PROFILE_PHONE_NUMBER: {
                result.setResult(false);
                result.setName("Đã tồn tại ứng viên có số điện thoại này");
                break;
            }
            default: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.NAME)));
                break;
            }
        }
    }

    private Bson getCondition(){
        switch (type) {
            case ThreadConfig.USER: {
                return Filters.eq(DbKeyConfig.USERNAME, this.id);
            }
            case ThreadConfig.PROFILE_EMAIL:
            case ThreadConfig.BLACKLIST_EMAIL: {
                return Filters.eq(DbKeyConfig.EMAIL, this.id);
            }
            case ThreadConfig.PROFILE_PHONE_NUMBER:
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                return Filters.eq(DbKeyConfig.PHONE_NUMBER, this.id);
            }
            default: {
                return Filters.eq(DbKeyConfig.ID, this.id);
            }
        }
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
            case ThreadConfig.USER: {
                return "username";
            }
            case ThreadConfig.NOTE: {
                return "id note";
            }
            case ThreadConfig.TALENT_POOL: {
                return "talent pool";
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
            case ThreadConfig.PROFILE_EMAIL:
            case ThreadConfig.PROFILE_PHONE_NUMBER:
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
            case ThreadConfig.USER: {
                return CollectionNameDefs.COLL_USER;
            }
            case ThreadConfig.NOTE: {
                return CollectionNameDefs.COLL_NOTE_PROFILE;
            }
            case ThreadConfig.BLACKLIST_EMAIL:
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                return CollectionNameDefs.COLL_BLACKLIST;
            }
            case ThreadConfig.TALENT_POOL: {
                return CollectionNameDefs.COLL_TALENT_POOL;
            }
            default: {
                return null;
            }
        }
    }

}
