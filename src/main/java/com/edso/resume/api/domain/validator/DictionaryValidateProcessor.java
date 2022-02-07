package com.edso.resume.api.domain.validator;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ThreadConfig;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import lombok.Data;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class DictionaryValidateProcessor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DictionaryValidatorResult result;
    private final MongoDbOnlineSyncActions db;
    private final IDictionaryValidator target;
    private final String key;
    private final String type;
    private final Object id;
    private String idProfile;
    private String recruitmentId;

    public DictionaryValidateProcessor(String key, String type, Object id, MongoDbOnlineSyncActions db, IDictionaryValidator target) {
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
            switch (type) {
                case ThreadConfig.LIST_USER: {
                    List<String> listString = (List<String>) id;
                    List<Document> lst = db.findAll(CollectionNameDefs.COLL_USER, Filters.in(DbKeyConfig.USERNAME, listString), null, 0, 0);
                    if (lst.size() != listString.size()) {
                        result.setResult(false);
                        result.setName("Không tồn tại username này!");
                        return;
                    }
                    List<Document> list = new ArrayList<>();
                    for (Document doc : lst) {
                        Document document = new Document();
                        document.append(DbKeyConfig.USERNAME, doc.get(DbKeyConfig.USERNAME));
                        document.append(DbKeyConfig.FULL_NAME, doc.get(DbKeyConfig.FULL_NAME));
                        document.append(DbKeyConfig.EMAIL, doc.get(DbKeyConfig.EMAIL));
                        list.add(document);
                    }
                    result.setResult(true);
                    result.setName(list);
                    return;
                }
                case ThreadConfig.LIST_TAG: {
                    List<String> listString = (List<String>) id;
                    List<Document> list = db.findAll(CollectionNameDefs.COLL_TAG, Filters.in(DbKeyConfig.ID, listString), null, 0, 0);
                    if (list.size() != listString.size()) {
                        result.setResult(false);
                        result.setName("Không tồn tại thẻ này!");
                        return;
                    }
                    result.setResult(true);
                    return;
                }
                case ThreadConfig.PROFILE_PHONE_NUMBER:
                case ThreadConfig.PROFILE_EMAIL: {
                    Bson cond = getCondition();
                    FindIterable<Document> list = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, 0, 0);
                    if (list == null) {
                        result.setResult(true);
                        return;
                    }
                    for (Document document : list) {
                        if (!AppUtils.parseString(document.get(DbKeyConfig.ID)).equals(idProfile)) {
                            result.setResult(true);
                            result.setName("Đã tồn tại ứng viên có email này");
                            return;
                        }
                    }
                    result.setResult(true);
                    return;
                }
                case ThreadConfig.LIST_SKILL: {
                    List<String> listString = (List<String>) id;
                    List<Document> lst = db.findAll(CollectionNameDefs.COLL_TAG, Filters.in(DbKeyConfig.ID, listString), null, 0, 0);
                    if (lst.size() != listString.size()) {
                        result.setResult(false);
                        result.setName("Không tồn tại kỹ năng công việc này!");
                        return;
                    }
                    List<Document> list = new ArrayList<>();
                    for (Document doc : lst) {
                        Document document = new Document();
                        document.append(DbKeyConfig.ID, doc.get(DbKeyConfig.ID));
                        document.append(DbKeyConfig.NAME, doc.get(DbKeyConfig.NAME));
                        list.add(document);
                    }
                    result.setResult(true);
                    result.setName(list);
                    return;
                }
                case ThreadConfig.STATUS_CV: {
                    Bson cond = Filters.and(Filters.eq(DbKeyConfig.ID, recruitmentId), Filters.eq("interview_process.id", id));
                    Document doc = db.findOne(getCollectionName(), cond);
                    if (doc == null) {
                        result.setResult(false);
                        result.setName("Không tồn tại vòng tuyển dụng này!");
                        return;
                    }
                    List<Document> list = (List<Document>) doc.get(DbKeyConfig.INTERVIEW_PROCESS);
                    if (list != null && !list.isEmpty()) {
                        for (Document roundEntity : list) {
                            if (roundEntity.get(DbKeyConfig.ID).equals(id)) {
                                result.setResult(true);
                                result.setName(roundEntity.get(DbKeyConfig.NAME));
                                return;
                            }
                        }
                    }
                    result.setResult(true);
                    return;
                }
                default: {
                    Bson cond = getCondition();
                    Document doc = db.findOne(getCollectionName(), cond);
                    if (doc == null) {
                        if (type.equals(ThreadConfig.BLACKLIST_EMAIL) || type.equals(ThreadConfig.BLACKLIST_PHONE_NUMBER) || type.equals(ThreadConfig.RECRUITMENT_NAME) || type.equals(ThreadConfig.FOLLOWER)) {
                            result.setResult(true);
                            return;
                        }
                        result.setResult(false);
                        result.setName("Không tồn tại " + getDictionaryName() + " này!");
                        return;
                    }
                    setResult(doc);
                    break;
                }
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

    private void setResult(Document doc) {
        switch (type) {
            case ThreadConfig.PROFILE: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)));
                result.setIdProfile(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_ID)));
                result.setStatusCVId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)));
                result.setDocument(doc);
                return;
            }
            case ThreadConfig.MERGE_PROFILE:
            case ThreadConfig.MERGE_OTHER_PROFILE: {
                result.setResult(true);
                result.setName(doc);
                return;
            }
            case ThreadConfig.TAG: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.NAME)));
                return;
            }
            case ThreadConfig.FOLLOWER: {
                result.setResult(false);
                result.setName("User đã theo dõi ứng viên này");
                return;
            }
            case ThreadConfig.CALENDAR_PROFILE: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)));
                result.setFullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)));
                result.setMailRef(AppUtils.parseString(doc.get(DbKeyConfig.MAIL_REF)));
                return;
            }
            case ThreadConfig.CHANGE_RECRUITMENT_PROFILE: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_ID)));
                result.setStatusCVId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)));
                return;
            }
            case ThreadConfig.REJECT_PROFILE: {
                if (AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)).equals("")) {
                    result.setResult(false);
                    result.setName("Không thể loại ứng viên đã bị loại!");
                    return;
                }
                result.setResult(true);
                result.setName(AppUtils.parseLong(doc.get(DbKeyConfig.RECRUITMENT_TIME)));
                result.setFullName(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)));
                result.setStatusCVId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)));
                return;
            }
            case ThreadConfig.USER: {
                result.setResult(true);
                result.setFullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)));
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)));
                return;
            }
            case ThreadConfig.NOTE: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.PATH_FILE)));
                result.setIdProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)));
                return;
            }
            case ThreadConfig.CALENDAR: {
                if (AppUtils.parseLong(doc.get(DbKeyConfig.DATE)) < System.currentTimeMillis()) {
                    result.setResult(false);
                    result.setName("Không được sửa lịch này!");
                    return;
                }
                result.setResult(true);
                result.setIdProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)));
                return;
            }
            case ThreadConfig.REASON: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.REASON)));
                return;
            }
            case ThreadConfig.RECRUITMENT: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.TITLE)));
                result.setFullName(AppUtils.parseString(doc.get(DbKeyConfig.CREATE_BY)));
                return;
            }
            case ThreadConfig.BLACKLIST_EMAIL: {
                result.setResult(false);
                result.setName("Email của ứng viên này đã tồn tại trong blacklist!");
                return;
            }
            case ThreadConfig.BLACKLIST_PHONE_NUMBER: {
                result.setResult(false);
                result.setName("Số điện thoại của ứng viên này đang trong blacklist!");
                return;
            }
            case ThreadConfig.RECRUITMENT_NAME: {
                if (!AppUtils.parseString(doc.get(DbKeyConfig.ID)).equals(recruitmentId)) {
                    result.setResult(false);
                    result.setName("Đã tồn tại tên tin tuyển dụng này!");
                    return;
                }
                result.setResult(true);
                return;
            }
            default: {
                result.setResult(true);
                result.setName(AppUtils.parseString(doc.get(DbKeyConfig.NAME)));
                break;
            }
        }
    }

    private Bson getCondition() {
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
            case ThreadConfig.RECRUITMENT_NAME: {
                return Filters.eq(DbKeyConfig.NAME_EQUAL, this.id);
            }
            case ThreadConfig.FOLLOWER: {
                return Filters.eq(DbKeyConfig.FOLLOWERS, this.id);
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
                return "cấp bậc công việc";
            }
            case ThreadConfig.SCHOOL: {
                return "trường học";
            }
            case ThreadConfig.SOURCE_CV: {
                return "nguồn cv";
            }
            case ThreadConfig.CALENDAR_PROFILE:
            case ThreadConfig.TALENT_POOL_PROFILE:
            case ThreadConfig.REJECT_PROFILE:
            case ThreadConfig.CHANGE_RECRUITMENT_PROFILE:
            case ThreadConfig.MERGE_PROFILE:
            case ThreadConfig.MERGE_OTHER_PROFILE:
            case ThreadConfig.PROFILE: {
                return "id profile";
            }
            case ThreadConfig.STATUS_CV: {
                return "vòng tuyền dụng";
            }
            case ThreadConfig.DEPARTMENT: {
                return "phòng ban";
            }
            case ThreadConfig.CALENDAR: {
                return "id calendar";
            }
            case ThreadConfig.LIST_USER:
            case ThreadConfig.USER: {
                return "username";
            }
            case ThreadConfig.NOTE: {
                return "id note";
            }
            case ThreadConfig.TALENT_POOL: {
                return "talent pool";
            }
            case ThreadConfig.RECRUITMENT: {
                return "tin tuyển dụng";
            }
            case ThreadConfig.LIST_SKILL: {
                return "kỹ năng công việc";
            }
            case ThreadConfig.ADDRESS: {
                return "địa chỉ";
            }
            case ThreadConfig.REASON: {
                return "lý do loại ứng viên";
            }
            case ThreadConfig.TAG: {
                return "thẻ";
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
            case ThreadConfig.FOLLOWER:
            case ThreadConfig.CALENDAR_PROFILE:
            case ThreadConfig.TALENT_POOL_PROFILE:
            case ThreadConfig.CHANGE_RECRUITMENT_PROFILE:
            case ThreadConfig.REJECT_PROFILE:
            case ThreadConfig.MERGE_PROFILE:
            case ThreadConfig.MERGE_OTHER_PROFILE:
            case ThreadConfig.PROFILE: {
                return CollectionNameDefs.COLL_PROFILE;
            }
            case ThreadConfig.DEPARTMENT: {
                return CollectionNameDefs.COLL_DEPARTMENT_COMPANY;
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
            case ThreadConfig.STATUS_CV:
            case ThreadConfig.RECRUITMENT_NAME:
            case ThreadConfig.RECRUITMENT: {
                return CollectionNameDefs.COLL_RECRUITMENT;
            }
            case ThreadConfig.LIST_SKILL: {
                return CollectionNameDefs.COLL_SKILL;
            }
            case ThreadConfig.ADDRESS: {
                return CollectionNameDefs.COLL_ADDRESS;
            }
            case ThreadConfig.REASON: {
                return CollectionNameDefs.COLL_REASON_REJECT;
            }
            case ThreadConfig.TAG: {
                return CollectionNameDefs.COLL_TAG;
            }
            default: {
                return null;
            }
        }
    }

}
