package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.EventEntity;
import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.entities.ProfileRabbitMQEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetReponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService, IDictionaryValidator {

    private final HistoryService historyService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();
    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate) {
        super(db);
        this.rabbitTemplate = rabbitTemplate;
        this.historyService = historyService;
    }

    private void insertToRabbitMQ(String type, Object obj) {
        EventEntity event = new EventEntity(type, obj);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, Integer page, Integer size) {

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(fullName.toLowerCase())));
        }

        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ProfileEntity> rows = new ArrayList<>();

        if (lst != null) {
            for (Document doc : lst) {
                ProfileEntity profile = ProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .dateOfBirth(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_BIRTH)))
                        .hometown(AppUtils.parseString(doc.get(DbKeyConfig.HOMETOWN)))
                        .schoolId(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_ID)))
                        .schoolName(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_NAME)))
                        .phoneNumber(AppUtils.parseString(doc.get(DbKeyConfig.PHONE_NUMBER)))
                        .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                        .jobId(AppUtils.parseString(doc.get(DbKeyConfig.JOB_ID)))
                        .jobName(AppUtils.parseString(doc.get(DbKeyConfig.JOB_NAME)))
                        .levelJobId(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_ID)))
                        .levelJobName(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_NAME)))
                        .cv(AppUtils.parseString(doc.get(DbKeyConfig.CV)))
                        .sourceCVId(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_ID)))
                        .sourceCVName(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_NAME)))
                        .hrRef(AppUtils.parseString(doc.get(DbKeyConfig.HR_REF)))
                        .dateOfApply(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_APPLY)))
                        .cvType(AppUtils.parseString(doc.get(DbKeyConfig.CV_TYPE)))
                        .statusCVId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)))
                        .statusCVName(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)))
                        .build();
                rows.add(profile);
            }
        }

        GetArrayResponse<ProfileEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);

        return resp;
    }

    @Override
    public GetReponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile) {

        GetReponse<ProfileDetailEntity> response = new GetReponse<>();

        //Validate
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idProfile));
        if (idProfileDocument == null) {
            response.setFailed("Id profile này không tồn tại");
            return response;
        }

        Bson cond = Filters.regex(DbKeyConfig.ID, Pattern.compile(idProfile));
        Document one = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
        ProfileDetailEntity profile = ProfileDetailEntity.builder()
                .id(AppUtils.parseString(one.get(DbKeyConfig.ID)))
                .fullName(AppUtils.parseString(one.get(DbKeyConfig.FULL_NAME)))
                .dateOfBirth(AppUtils.parseLong(one.get(DbKeyConfig.DATE_OF_BIRTH)))
                .hometown(AppUtils.parseString(one.get(DbKeyConfig.HOMETOWN)))
                .schoolId(AppUtils.parseString(one.get(DbKeyConfig.SCHOOL_ID)))
                .schoolName(AppUtils.parseString(one.get(DbKeyConfig.SCHOOL_NAME)))
                .phoneNumber(AppUtils.parseString(one.get(DbKeyConfig.PHONE_NUMBER)))
                .email(AppUtils.parseString(one.get(DbKeyConfig.EMAIL)))
                .jobId(AppUtils.parseString(one.get(DbKeyConfig.JOB_ID)))
                .jobName(AppUtils.parseString(one.get(DbKeyConfig.JOB_NAME)))
                .levelJobId(AppUtils.parseString(one.get(DbKeyConfig.LEVEL_JOB_ID)))
                .levelJobName(AppUtils.parseString(one.get(DbKeyConfig.LEVEL_JOB_NAME)))
                .cv(AppUtils.parseString(one.get(DbKeyConfig.CV)))
                .sourceCVId(AppUtils.parseString(one.get(DbKeyConfig.SOURCE_CV_ID)))
                .sourceCVName(AppUtils.parseString(one.get(DbKeyConfig.SOURCE_CV_NAME)))
                .hrRef(AppUtils.parseString(one.get(DbKeyConfig.HR_REF)))
                .dateOfApply(AppUtils.parseLong(one.get(DbKeyConfig.DATE_OF_APPLY)))
                .cvType(AppUtils.parseString(one.get(DbKeyConfig.CV_TYPE)))
                .statusCVId(AppUtils.parseString(one.get(DbKeyConfig.STATUS_CV_ID)))
                .statusCVName(AppUtils.parseString(one.get(DbKeyConfig.STATUS_CV_NAME)))
                .lastApply(AppUtils.parseLong(one.get(DbKeyConfig.LAST_APPLY)))
                .tags(AppUtils.parseString(one.get(DbKeyConfig.TAGS)))
                .gender(AppUtils.parseString(one.get(DbKeyConfig.GENDER)))
                .note(AppUtils.parseString(one.get(DbKeyConfig.NOTE)))
                .dateOfCreate(AppUtils.parseLong(one.get(DbKeyConfig.CREATE_AT)))
                .dateOfUpdate(AppUtils.parseLong(one.get(DbKeyConfig.UPDATE_AT)))
                .evaluation(AppUtils.parseString(one.get(DbKeyConfig.EVALUATION)))
                .build();

        response.setSuccess(profile);

        //Insert history to DB
        historyService.createHistory(idProfile, TypeConfig.SELECT, "Xem chi tiết profile", info.getUsername());

        return response;
    }

    @Override
    public BaseResponse createProfile(CreateProfileRequest request) {

        BaseResponse response = new BaseResponse();

        String idProfile = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        try {

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            int total = rs.size();

            for (DictionaryValidateProcessor p : rs) {
                Thread t = new Thread(p);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult result = queue.poll();
                if (result != null) {
                    if (result.getKey().equals(key)) {
                        if (!result.isResult()) {
                            response.setFailed(result.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(result);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            String schoolName = null;
            String jobName = null;
            String levelJobName = null;
            String sourceCVName = null;

            for (DictionaryValidateProcessor r : rs) {
                switch (r.getResult().getType()) {
                    case ThreadConfig.JOB: {
                        jobName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.JOB_LEVEL: {
                        levelJobName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.SCHOOL: {
                        schoolName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.SOURCE_CV: {
                        sourceCVName = r.getResult().getName();
                        break;
                    }
                }
            }

            // conventions
            Document profile = new Document();
            profile.append(DbKeyConfig.ID, idProfile);
            profile.append(DbKeyConfig.FULL_NAME, request.getFullName());
            profile.append(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber());
            profile.append(DbKeyConfig.EMAIL, request.getEmail());
            profile.append(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth());
            profile.append(DbKeyConfig.HOMETOWN, request.getHometown());
            profile.append(DbKeyConfig.SCHOOL_ID, request.getSchool());
            profile.append(DbKeyConfig.SCHOOL_NAME, schoolName);
            profile.append(DbKeyConfig.JOB_ID, request.getJob());
            profile.append(DbKeyConfig.JOB_NAME, jobName);
            profile.append(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob());
            profile.append(DbKeyConfig.LEVEL_JOB_NAME, levelJobName);
            profile.append(DbKeyConfig.CV, request.getCv());
            profile.append(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV());
            profile.append(DbKeyConfig.SOURCE_CV_NAME, sourceCVName);
            profile.append(DbKeyConfig.HR_REF, request.getHrRef());
            profile.append(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply());
            profile.append(DbKeyConfig.CV_TYPE, request.getCvType());
            profile.append(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase());
            profile.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            profile.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            profile.append(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis());
            profile.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            profile.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());
            profile.append(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_PROFILE, profile);

            ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
            profileEntity.setId(idProfile);
            profileEntity.setFullName(request.getFullName());
            profileEntity.setPhoneNumber(request.getPhoneNumber());
            profileEntity.setEmail(request.getEmail());
            profileEntity.setDateOfBirth(request.getDateOfBirth());
            profileEntity.setHometown(request.getHometown());
            profileEntity.setSchoolId(request.getSchool());
            profileEntity.setSchoolName(schoolName);
            profileEntity.setJobId(request.getJob());
            profileEntity.setJobName(jobName);
            profileEntity.setLevelJobId(request.getLevelJob());
            profileEntity.setLevelJobName(levelJobName);
            profileEntity.setCv(request.getCv());
            profileEntity.setSourceCVId(request.getSourceCV());
            profileEntity.setSourceCVName(sourceCVName);
            profileEntity.setHrRef(request.getHrRef());
            profileEntity.setDateOfApply(request.getDateOfApply());
            profileEntity.setCvType(request.getCvType());

            // insert to rabbitmq
            insertToRabbitMQ("create", profileEntity);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.CREATE, "Tạo profile", request.getInfo().getUsername());

            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }

    }

    @Override
    public BaseResponse updateProfile(UpdateProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {

            //Validate
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            int total = rs.size();

            for (DictionaryValidateProcessor r : rs) {
                Thread t = new Thread(r);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult validatorResult = queue.poll();
                if (validatorResult != null) {
                    if (validatorResult.getKey().equals(key)) {
                        if (!validatorResult.isResult()) {
                            response.setFailed(validatorResult.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(validatorResult);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            String schoolName = null;
            String jobName = null;
            String levelJobName = null;
            String sourceCVName = null;

            for (DictionaryValidateProcessor r : rs) {
                switch (r.getResult().getType()) {
                    case ThreadConfig.JOB: {
                        jobName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.JOB_LEVEL: {
                        levelJobName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.SCHOOL: {
                        schoolName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.SOURCE_CV: {
                        sourceCVName = r.getResult().getName();
                        break;
                    }
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                    Updates.set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                    Updates.set(DbKeyConfig.HOMETOWN, request.getHometown()),
                    Updates.set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                    Updates.set(DbKeyConfig.SCHOOL_NAME, schoolName),
                    Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                    Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                    Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
                    Updates.set(DbKeyConfig.JOB_NAME, jobName),
                    Updates.set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                    Updates.set(DbKeyConfig.LEVEL_JOB_NAME, levelJobName),
                    Updates.set(DbKeyConfig.CV, request.getCv()),
                    Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, sourceCVName),
                    Updates.set(DbKeyConfig.HR_REF, request.getHrRef()),
                    Updates.set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    Updates.set(DbKeyConfig.CV_TYPE, request.getCvType()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            response.setSuccess();

            ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
            profileEntity.setId(id);
            profileEntity.setFullName(request.getFullName());
            profileEntity.setPhoneNumber(request.getPhoneNumber());
            profileEntity.setEmail(request.getEmail());
            profileEntity.setDateOfBirth(request.getDateOfBirth());
            profileEntity.setHometown(request.getHometown());
            profileEntity.setSchoolId(request.getSchool());
            profileEntity.setSchoolName(schoolName);
            profileEntity.setJobId(request.getJob());
            profileEntity.setJobName(jobName);
            profileEntity.setLevelJobId(request.getLevelJob());
            profileEntity.setLevelJobName(levelJobName);
            profileEntity.setCv(request.getCv());
            profileEntity.setSourceCVId(request.getSourceCV());
            profileEntity.setSourceCVName(sourceCVName);
            profileEntity.setHrRef(request.getHrRef());
            profileEntity.setDateOfApply(request.getDateOfApply());
            profileEntity.setCvType(request.getCvType());

            // insert to rabbitmq
            insertToRabbitMQ("update", profileEntity);

            //Insert history to DB
            historyService.createHistory(id, TypeConfig.UPDATE, "Sửa profile", request.getInfo().getUsername());

            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
    }

    @Override
    public BaseResponse updateDetailProfile(UpdateDetailProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            int total = rs.size();

            for (DictionaryValidateProcessor r : rs) {
                Thread t = new Thread(r);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult validatorResult = queue.poll();
                if (validatorResult != null) {
                    if (validatorResult.getKey().equals(key)) {
                        if (!validatorResult.isResult()) {
                            response.setFailed(validatorResult.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(validatorResult);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            String schoolName = null;
            String jobName = null;
            String levelJobName = null;
            String sourceCVName = null;

            for (DictionaryValidateProcessor r : rs) {
                switch (r.getResult().getType()) {
                    case ThreadConfig.JOB: {
                        jobName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.JOB_LEVEL: {
                        levelJobName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.SCHOOL: {
                        schoolName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.SOURCE_CV: {
                        sourceCVName = r.getResult().getName();
                        break;
                    }
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                    Updates.set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                    Updates.set(DbKeyConfig.HOMETOWN, request.getHometown()),
                    Updates.set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                    Updates.set(DbKeyConfig.SCHOOL_NAME, schoolName),
                    Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                    Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                    Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
                    Updates.set(DbKeyConfig.JOB_NAME, jobName),
                    Updates.set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                    Updates.set(DbKeyConfig.LEVEL_JOB_NAME, levelJobName),
                    Updates.set(DbKeyConfig.CV, request.getCv()),
                    Updates.set(DbKeyConfig.TAGS, request.getTags()),
                    Updates.set(DbKeyConfig.NOTE, request.getNote()),
                    Updates.set(DbKeyConfig.GENDER, request.getGender()),
                    Updates.set(DbKeyConfig.LAST_APPLY, request.getLastApply()),
                    Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                    Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, sourceCVName),
                    Updates.set(DbKeyConfig.HR_REF, request.getHrRef()),
                    Updates.set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    Updates.set(DbKeyConfig.CV_TYPE, request.getCvType()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            response.setSuccess();

            ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
            profileEntity.setId(id);
            profileEntity.setFullName(request.getFullName());
            profileEntity.setPhoneNumber(request.getPhoneNumber());
            profileEntity.setEmail(request.getEmail());
            profileEntity.setDateOfBirth(request.getDateOfBirth());
            profileEntity.setHometown(request.getHometown());
            profileEntity.setSchoolId(request.getSchool());
            profileEntity.setSchoolName(schoolName);
            profileEntity.setJobId(request.getJob());
            profileEntity.setJobName(jobName);
            profileEntity.setLevelJobId(request.getLevelJob());
            profileEntity.setLevelJobName(levelJobName);
            profileEntity.setCv(request.getCv());
            profileEntity.setSourceCVId(request.getSourceCV());
            profileEntity.setSourceCVName(sourceCVName);
            profileEntity.setHrRef(request.getHrRef());
            profileEntity.setDateOfApply(request.getDateOfApply());
            profileEntity.setCvType(request.getCvType());

            // insert to rabbitmq
            insertToRabbitMQ("update detail profile", request);

            //Insert history to DB
            historyService.createHistory(id, TypeConfig.UPDATE, "Sửa chi tiết profile", request.getInfo().getUsername());

            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }

    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }

    @Override
    public BaseResponse deleteProfile(DeleteProfileRequest request) {

        BaseResponse response = new BaseResponse();

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        // insert to rabbitmq
        insertToRabbitMQ("delete", request);

        //Insert history to DB
        historyService.createHistory(id, TypeConfig.DELETE, "Xóa profile", request.getInfo().getUsername());
        response.setSuccess();

        return response;
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {

            //Validate
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.STATUS_CV, request.getStatusCV(), db, this));
            int total = rs.size();

            for (DictionaryValidateProcessor r : rs) {
                Thread t = new Thread(r);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                DictionaryValidatorResult validatorResult = queue.poll();
                if (validatorResult != null) {
                    if (validatorResult.getKey().equals(key)) {
                        if (!validatorResult.isResult()) {
                            response.setFailed(validatorResult.getName());
                            return response;
                        } else {
                            count++;
                        }
                        total--;
                    } else {
                        queue.offer(validatorResult);
                    }
                }
            }

            if (count != rs.size()) {
                for (DictionaryValidateProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        response.setFailed("Không thể kiếm tra: " + r.getResult().getType());
                        return response;
                    }
                }
            }

            String statusCVName = null;

            for (DictionaryValidateProcessor r : rs) {
                if (r.getResult().getType().equals(ThreadConfig.STATUS_CV)) {
                    statusCVName = r.getResult().getName();
                }
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.STATUS_CV_ID, request.getStatusCV()),
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, statusCVName),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            response.setSuccess();

            ProfileRabbitMQEntity profileRabbitMQ = new ProfileRabbitMQEntity();
            profileRabbitMQ.setStatusCVId(request.getStatusCV());
            profileRabbitMQ.setStatusCVName(statusCVName);

            // insert to rabbitmq
            insertToRabbitMQ("updateStatus", profileRabbitMQ);

            //Insert history to DB
            historyService.createHistory(id, TypeConfig.UPDATE, "Cập nhật trạng thái profile", request.getInfo().getUsername());

            return response;

        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        } finally {
            synchronized (queue) {
                queue.removeIf(s -> s.getKey().equals(key));
            }
        }
    }


}
