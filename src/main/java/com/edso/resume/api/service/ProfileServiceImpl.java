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
import com.edso.resume.lib.response.GetResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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
    private final CalendarService calendarService;
    private final NoteService noteService;

    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate, CalendarService calendarService, NoteService noteService) {
        super(db);
        this.historyService = historyService;
        this.rabbitTemplate = rabbitTemplate;
        this.calendarService = calendarService;
        this.noteService = noteService;
    }

    private void publishActionToRabbitMQ(String type, Object obj) {
        EventEntity event = new EventEntity(type, obj);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
        logger.info("=>publishActionToRabbitMQ type: {}, profile: {}", type, obj);
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, String talentPool, Integer page, Integer size) {

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(fullName.toLowerCase())));
        }
        if (!Strings.isNullOrEmpty(talentPool)) {
            c.add(Filters.regex(DbKeyConfig.TALENT_POOL_ID, Pattern.compile(talentPool)));
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
                        .gender(AppUtils.parseString(doc.get(DbKeyConfig.GENDER)))
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
                        .sourceCVId(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_ID)))
                        .sourceCVName(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_NAME)))
                        .hrRef(AppUtils.parseString(doc.get(DbKeyConfig.HR_REF)))
                        .dateOfApply(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_APPLY)))
                        .statusCVId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)))
                        .statusCVName(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)))
                        .talentPoolId(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_ID)))
                        .talentPoolName(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_NAME)))
                        .image(AppUtils.parseString(doc.get(DbKeyConfig.URL_IMAGE)))
                        .cv(AppUtils.parseString(doc.get(DbKeyConfig.CV)))
                        .urlCV(AppUtils.parseString(doc.get(DbKeyConfig.URL_CV)))
                        .departmentId(AppUtils.parseString(doc.get(DbKeyConfig.DEPARTMENT_ID)))
                        .departmentName(AppUtils.parseString(doc.get(DbKeyConfig.DEPARTMENT_NAME)))
                        .levelSchool(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_SCHOOL)))
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
    public GetResponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile) {

        GetResponse<ProfileDetailEntity> response = new GetResponse<>();

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
                .sourceCVId(AppUtils.parseString(one.get(DbKeyConfig.SOURCE_CV_ID)))
                .sourceCVName(AppUtils.parseString(one.get(DbKeyConfig.SOURCE_CV_NAME)))
                .hrRef(AppUtils.parseString(one.get(DbKeyConfig.HR_REF)))
                .dateOfApply(AppUtils.parseLong(one.get(DbKeyConfig.DATE_OF_APPLY)))
                .statusCVId(AppUtils.parseString(one.get(DbKeyConfig.STATUS_CV_ID)))
                .statusCVName(AppUtils.parseString(one.get(DbKeyConfig.STATUS_CV_NAME)))
                .lastApply(AppUtils.parseLong(one.get(DbKeyConfig.LAST_APPLY)))
                .gender(AppUtils.parseString(one.get(DbKeyConfig.GENDER)))
                .dateOfCreate(AppUtils.parseLong(one.get(DbKeyConfig.CREATE_AT)))
                .dateOfUpdate(AppUtils.parseLong(one.get(DbKeyConfig.UPDATE_AT)))
                .evaluation(AppUtils.parseString(one.get(DbKeyConfig.EVALUATION)))
                .talentPoolId(AppUtils.parseString(one.get(DbKeyConfig.TALENT_POOL_ID)))
                .talentPoolName(AppUtils.parseString(one.get(DbKeyConfig.TALENT_POOL_NAME)))
                .image(AppUtils.parseString(one.get(DbKeyConfig.URL_IMAGE)))
                .urlCV(AppUtils.parseString(one.get(DbKeyConfig.URL_CV)))
                .departmentId(AppUtils.parseString(one.get(DbKeyConfig.DEPARTMENT_ID)))
                .departmentName(AppUtils.parseString(one.get(DbKeyConfig.DEPARTMENT_NAME)))
                .levelSchool(AppUtils.parseString(one.get(DbKeyConfig.LEVEL_SCHOOL)))
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
            if (!Strings.isNullOrEmpty(request.getSchool())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.DEPARTMENT, request.getDepartment(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber(), db, this));
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
            String talentPoolName = null;
            String departmentName = null;

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
                    case ThreadConfig.TALENT_POOL: {
                        talentPoolName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.DEPARTMENT: {
                        departmentName = r.getResult().getName();
                        break;
                    }
                }
            }

            // conventions
            Document profile = new Document();
            profile.append(DbKeyConfig.ID, idProfile);
            profile.append(DbKeyConfig.FULL_NAME, request.getFullName());
            profile.append(DbKeyConfig.GENDER, request.getGender());
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
            profile.append(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV());
            profile.append(DbKeyConfig.SOURCE_CV_NAME, sourceCVName);
            profile.append(DbKeyConfig.HR_REF, request.getHrRef());
            profile.append(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply());
            profile.append(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase());
            profile.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            profile.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            profile.append(DbKeyConfig.TALENT_POOL_ID, request.getTalentPool());
            profile.append(DbKeyConfig.TALENT_POOL_NAME, talentPoolName);
            profile.append(DbKeyConfig.DEPARTMENT_ID, request.getDepartment());
            profile.append(DbKeyConfig.DEPARTMENT_NAME, departmentName);
            profile.append(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_PROFILE, profile);

            ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
            profileEntity.setId(idProfile);
            profileEntity.setFullName(request.getFullName());
            profileEntity.setGender(request.getGender());
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
            profileEntity.setSourceCVId(request.getSourceCV());
            profileEntity.setSourceCVName(sourceCVName);
            profileEntity.setHrRef(request.getHrRef());
            profileEntity.setDateOfApply(request.getDateOfApply());
            profileEntity.setTalentPoolId(request.getTalentPool());
            profileEntity.setTalentPoolName(talentPoolName);
            profileEntity.setDepartmentId(request.getDepartment());
            profileEntity.setDepartmentName(departmentName);
            profileEntity.setDepartmentName(request.getLevelSchool());

            // insert to rabbitmq
            publishActionToRabbitMQ("create", profileEntity);

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
            if (!Strings.isNullOrEmpty(request.getSchool())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.DEPARTMENT, request.getDepartment(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber(), db, this));
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
            String talentPoolName = null;
            String departmentName = null;
            String email = null;

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
                    case ThreadConfig.TALENT_POOL: {
                        talentPoolName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.DEPARTMENT: {
                        departmentName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.PROFILE: {
                        email = r.getResult().getName();
                        break;
                    }
                }
            }

            //Update coll calendar
            if (!email.equals(request.getEmail())) {
                Bson idProfile = Filters.eq(DbKeyConfig.ID_PROFILE, request.getId());
                Bson updateProfile = Updates.combine(
                        Updates.set(DbKeyConfig.EMAIL, request.getEmail())
                );
                db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, idProfile, updateProfile, true);
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                    Updates.set(DbKeyConfig.GENDER, request.getGender()),
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
                    Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, sourceCVName),
                    Updates.set(DbKeyConfig.HR_REF, request.getHrRef()),
                    Updates.set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    Updates.set(DbKeyConfig.TALENT_POOL_ID, request.getTalentPool()),
                    Updates.set(DbKeyConfig.TALENT_POOL_NAME, talentPoolName),
                    Updates.set(DbKeyConfig.DEPARTMENT_ID, request.getDepartment()),
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, departmentName),
                    Updates.set(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            response.setSuccess();

            ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
            profileEntity.setId(id);
            profileEntity.setFullName(request.getFullName());
            profileEntity.setGender(request.getGender());
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
            profileEntity.setSourceCVId(request.getSourceCV());
            profileEntity.setSourceCVName(sourceCVName);
            profileEntity.setHrRef(request.getHrRef());
            profileEntity.setDateOfApply(request.getDateOfApply());
            profileEntity.setTalentPoolId(request.getTalentPool());
            profileEntity.setTalentPoolName(talentPoolName);
            profileEntity.setDepartmentId(request.getDepartment());
            profileEntity.setDepartmentName(departmentName);
            profileEntity.setLevelSchool(request.getLevelSchool());

            // insert to rabbitmq
            publishActionToRabbitMQ("update", profileEntity);

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
            if (!Strings.isNullOrEmpty(request.getSchool())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.DEPARTMENT, request.getDepartment(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, id, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber(), db, this));
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
            String talentPoolName = null;
            String departmentName = null;
            String email = null;

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
                    case ThreadConfig.TALENT_POOL: {
                        talentPoolName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.DEPARTMENT: {
                        departmentName = r.getResult().getName();
                        break;
                    }
                    case ThreadConfig.PROFILE: {
                        email = r.getResult().getName();
                        break;
                    }
                }
            }

            //Update coll calendar
            if (!email.equals(request.getEmail())) {
                Bson idProfile = Filters.eq(DbKeyConfig.ID_PROFILE, request.getId());
                Bson updateProfile = Updates.combine(
                        Updates.set(DbKeyConfig.EMAIL, request.getEmail())
                );
                db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, idProfile, updateProfile, true);
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
                    Updates.set(DbKeyConfig.GENDER, request.getGender()),
                    Updates.set(DbKeyConfig.LAST_APPLY, request.getLastApply()),
                    Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                    Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, sourceCVName),
                    Updates.set(DbKeyConfig.HR_REF, request.getHrRef()),
                    Updates.set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    Updates.set(DbKeyConfig.TALENT_POOL_ID, request.getTalentPool()),
                    Updates.set(DbKeyConfig.TALENT_POOL_NAME, talentPoolName),
                    Updates.set(DbKeyConfig.DEPARTMENT_ID, request.getDepartment()),
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, departmentName),
                    Updates.set(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            response.setSuccess();

            ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
            profileEntity.setId(id);
            profileEntity.setFullName(request.getFullName());
            profileEntity.setGender(request.getGender());
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
            profileEntity.setSourceCVId(request.getSourceCV());
            profileEntity.setSourceCVName(sourceCVName);
            profileEntity.setHrRef(request.getHrRef());
            profileEntity.setDateOfApply(request.getDateOfApply());
            profileEntity.setTalentPoolId(request.getTalentPool());
            profileEntity.setTalentPoolName(talentPoolName);
            profileEntity.setEvaluation(request.getEvaluation());
            profileEntity.setLastApply(request.getLastApply());
            profileEntity.setDepartmentId(request.getDepartment());
            profileEntity.setDepartmentName(departmentName);
            profileEntity.setLevelSchool(request.getLevelSchool());

            // insert to rabbitmq
            publishActionToRabbitMQ("update-detail", profileEntity);

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
    public BaseResponse deleteProfile(DeleteProfileRequest request) {

        BaseResponse response = new BaseResponse();

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Không tồn tại id profile này");
            return response;
        }

        deleteFile(AppUtils.parseString(idDocument.get(DbKeyConfig.URL_CV)));

        //Xóa profile
        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        //Xóa lịch phỏng vấn
        calendarService.deleteCalendarByIdProfile(request.getId());

        //Xóa note
        noteService.deleteNoteProfileByIdProfile(request.getId());

        //Xóa lịch sử
        historyService.deleteHistory(id);

        // insert to rabbitmq
        publishActionToRabbitMQ("delete", request);

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
            publishActionToRabbitMQ("update-status", profileRabbitMQ);

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

    public void deleteFile(String path) {
        File file = new File(path);
        if (file.delete()) {
            logger.info("deleteFile filePath:{}", path);
        }
    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }

}
