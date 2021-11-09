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
            Bson cond = getCondition();
            Document doc = db.findOne(getCollectionName(), cond);
            if (doc == null) {
                if (type.equals(ThreadConfig.BLACKLIST_EMAIL) || type.equals(ThreadConfig.BLACKLIST_PHONE_NUMBER) || type.equals(ThreadConfig.PROFILE_EMAIL )|| type.equals(ThreadConfig.PROFILE_PHONE_NUMBER)) {
                    result.setResult(true);
                    return;
                }
                result.setResult(false);
                logger.info("Không tồn tại "+getDictionaryName()+" này! name: {}", this.name);
                return;
            }

            setResult(doc);

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

    private void setResult(Document doc){
        switch (type){
            case ThreadConfig.BLACKLIST_EMAIL:{
                logger.info("Ứng viên có email này đang trong blacklist! email: {}", this.name);
                result.setResult(false);
                break;
            }
            case ThreadConfig.BLACKLIST_PHONE_NUMBER:{
                logger.info("Ứng viên có số điện thoại này đang trong blacklist! phoneNumber: {}", this.name);
                result.setResult(false);
                break;
            }
            case ThreadConfig.PROFILE_EMAIL:{
                result.setResult(false);
                logger.info("Đã có ứng viên dùng email này! email: {}", this.name);
                break;
            }
            case ThreadConfig.PROFILE_PHONE_NUMBER: {
                result.setResult(false);
                logger.info("Đã có ứng viên dùng số điện thoại này! phoneNumber: {}", this.name);
                break;
            }
            default:{
                result.setResult(true);
                result.setId(AppUtils.parseString(doc.get(DbKeyConfig.ID)));
            }
        }
    }

    private Bson getCondition(){
        switch (type) {
            case ThreadConfig.PROFILE_EMAIL:
            case ThreadConfig.BLACKLIST_EMAIL: {
                return Filters.eq(DbKeyConfig.EMAIL, this.name);
            }
            case ThreadConfig.PROFILE_PHONE_NUMBER:
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                return Filters.eq(DbKeyConfig.PHONE_NUMBER, this.name);
            }
            default: {
                return Filters.eq(DbKeyConfig.NAME, this.name);
            }
        }
    }

    private String getDictionaryName() {
        switch (type) {
            case ThreadConfig.JOB_LEVEL: {
                return "vị trí tuyển dụng";
            }
            case ThreadConfig.SCHOOL: {
                return "trường học";
            }
            case ThreadConfig.SOURCE_CV: {
                return "nguồn cv";
            }
            case ThreadConfig.DEPARTMENT: {
                return "phòng ban";
            }
            case ThreadConfig.TALENT_POOL: {
                return "talent pool";
            }
            default: {
                logger.info("Không có dictionary này!");
                return null;
            }
        }
    }

    private String getCollectionName() {
        switch (type) {
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
            case ThreadConfig.PROFILE_PHONE_NUMBER: {
                return CollectionNameDefs.COLL_PROFILE;
            }
            case ThreadConfig.DEPARTMENT: {
                return CollectionNameDefs.COLL_DEPARTMENT_COMPANY;
            }
            case ThreadConfig.BLACKLIST_EMAIL:
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                return CollectionNameDefs.COLL_BLACKLIST;
            }
            case ThreadConfig.TALENT_POOL: {
                return CollectionNameDefs.COLL_TALENT_POOL;
            }
            default: {
                logger.info("Không có collection này!");
                return null;
            }
        }
    }
}
