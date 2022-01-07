package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.*;
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

    private void publishActionToRabbitMQ(String type, Object profile) {
        EventEntity event = new EventEntity(type, profile);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
        logger.info("=>publishActionToRabbitMQ type: {}, profile: {}", type, profile);
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, String talentPool, String job, String levelJob, String department, String recruitment, String calendar, String statusCV, Integer page, Integer size) {

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(parseVietnameseToEnglish(fullName))));
        }
        if (!Strings.isNullOrEmpty(talentPool)) {
            c.add(Filters.eq(DbKeyConfig.TALENT_POOL_ID, talentPool));
        }
        if (!Strings.isNullOrEmpty(job)) {
            c.add(Filters.eq(DbKeyConfig.JOB_ID, job));
        }
        if (!Strings.isNullOrEmpty(levelJob)) {
            c.add(Filters.eq(DbKeyConfig.LEVEL_JOB_ID, levelJob));
        }
        if (!Strings.isNullOrEmpty(department)) {
            c.add(Filters.eq(DbKeyConfig.DEPARTMENT_ID, department));
        }
        if (!Strings.isNullOrEmpty(recruitment)) {
            c.add(Filters.eq(DbKeyConfig.RECRUITMENT_ID, recruitment));
        }
        if (!Strings.isNullOrEmpty(statusCV)) {
            c.add(Filters.eq(DbKeyConfig.STATUS_CV_ID, statusCV));
        }
        if (!Strings.isNullOrEmpty(calendar)) {
            if (calendar.equals("set")) {
                c.add(Filters.eq(DbKeyConfig.CALENDAR, -1));
            }
            if (calendar.equals("notset")) {
                c.add(Filters.ne(DbKeyConfig.CALENDAR, 1));
            }
        }
        Bson cond = buildCondition(c);
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
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
//                      .talentPool((List<TalentPool>) one.get(DbKeyConfig.TALENT_POOL))
                        .talentPoolId(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_ID)))
                        .talentPoolName(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_NAME)))
                        .image(AppUtils.parseString(doc.get(DbKeyConfig.URL_IMAGE)))
                        .cv(AppUtils.parseString(doc.get(DbKeyConfig.CV)))
                        .urlCV(AppUtils.parseString(doc.get(DbKeyConfig.URL_CV)))
                        .departmentId(AppUtils.parseString(doc.get(DbKeyConfig.DEPARTMENT_ID)))
                        .departmentName(AppUtils.parseString(doc.get(DbKeyConfig.DEPARTMENT_NAME)))
                        .levelSchool(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_SCHOOL)))
                        .recruitmentId(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_ID)))
                        .recruitmentName(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_NAME)))
                        .mailRef(AppUtils.parseString(doc.get(DbKeyConfig.MAIL_REF)))
                        .skill((List<SkillEntity>) doc.get(DbKeyConfig.SKILL))
                        .avatarColor(AppUtils.parseString(doc.get(DbKeyConfig.AVATAR_COLOR)))
                        .isNew(AppUtils.parseString(doc.get(DbKeyConfig.IS_NEW)))
                        .build();
                rows.add(profile);
            }
        }

        GetArrayResponse<ProfileEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_PROFILE, cond));
        resp.setRows(rows);

        return resp;
    }

    @Override
    public GetResponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile) {

        GetResponse<ProfileDetailEntity> response = new GetResponse<>();
        Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);
        //Validate
        Document one = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
        if (one == null) {
            response.setFailed("Id profile này không tồn tại");
            return response;
        }

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
//                .talentPool((List<TalentPool>) one.get(DbKeyConfig.TALENT_POOL))
                .talentPoolId(AppUtils.parseString(one.get(DbKeyConfig.TALENT_POOL_ID)))
                .talentPoolName(AppUtils.parseString(one.get(DbKeyConfig.TALENT_POOL_NAME)))
                .image(AppUtils.parseString(one.get(DbKeyConfig.URL_IMAGE)))
                .urlCV(AppUtils.parseString(one.get(DbKeyConfig.URL_CV)))
                .departmentId(AppUtils.parseString(one.get(DbKeyConfig.DEPARTMENT_ID)))
                .departmentName(AppUtils.parseString(one.get(DbKeyConfig.DEPARTMENT_NAME)))
                .levelSchool(AppUtils.parseString(one.get(DbKeyConfig.LEVEL_SCHOOL)))
                .recruitmentId(AppUtils.parseString(one.get(DbKeyConfig.RECRUITMENT_ID)))
                .recruitmentName(AppUtils.parseString(one.get(DbKeyConfig.RECRUITMENT_NAME)))
                .mailRef(AppUtils.parseString(one.get(DbKeyConfig.MAIL_REF)))
                .skill((List<SkillEntity>) one.get(DbKeyConfig.SKILL))
                .avatarColor(AppUtils.parseString(one.get(DbKeyConfig.AVATAR_COLOR)))
                .build();

        response.setSuccess(profile);

        //Insert history to DB
        historyService.createHistory(idProfile, TypeConfig.SELECT, "Xem chi tiết thông tin ứng viên", info);

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
            if (!Strings.isNullOrEmpty(request.getDepartment())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.DEPARTMENT, request.getDepartment(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getLevelJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            }
            if (request.getSkill() != null && !request.getSkill().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_SKILL, request.getSkill(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail(), db, this));
            if (!Strings.isNullOrEmpty(request.getPhoneNumber())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorPhoneNumber = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber(), db, this);
                dictionaryValidateProcessorPhoneNumber.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorPhoneNumber);
            }
            DictionaryValidateProcessor dictionaryValidateProcessorEmail = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail(), db, this);
            dictionaryValidateProcessorEmail.setIdProfile(idProfile);
            rs.add(dictionaryValidateProcessorEmail);
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
                            response.setFailed((String) result.getName());
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

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);

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
            profile.append(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName());
            profile.append(DbKeyConfig.JOB_ID, request.getJob());
            profile.append(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName());
            profile.append(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob());
            profile.append(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName());
            profile.append(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV());
            profile.append(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName());
            profile.append(DbKeyConfig.HR_REF, request.getHrRef());
            profile.append(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply());
            profile.append(DbKeyConfig.NAME_SEARCH, parseVietnameseToEnglish(request.getFullName()));
            profile.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            profile.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            profile.append(DbKeyConfig.DEPARTMENT_ID, request.getDepartment());
            profile.append(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName());
            profile.append(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool());
            profile.append(DbKeyConfig.MAIL_REF, request.getMailRef());
            profile.append(DbKeyConfig.SKILL, dictionaryNames.getSkill());
            profile.append(DbKeyConfig.AVATAR_COLOR, request.getAvatarColor());
            profile.append(DbKeyConfig.IS_NEW, true);

            // insert to rabbitmq
            ProfileRabbitMQEntity profileRabbitMQ = getProfileRabbit(idProfile, request, dictionaryNames);
            publishActionToRabbitMQ(RabbitMQConfig.CREATE, profileRabbitMQ);
            // insert to database
            db.insertOne(CollectionNameDefs.COLL_PROFILE, profile);
            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.CREATE, "Thêm ứng viên", request.getInfo());

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
            String idProfile = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            if (!Strings.isNullOrEmpty(request.getSchool())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getDepartment())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.DEPARTMENT, request.getDepartment(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getLevelJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            }
            if (request.getSkill() != null && !request.getSkill().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_SKILL, request.getSkill(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, request.getId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail(), db, this));
            if (!Strings.isNullOrEmpty(request.getPhoneNumber())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorPhoneNumber = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber(), db, this);
                dictionaryValidateProcessorPhoneNumber.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorPhoneNumber);
            }
            DictionaryValidateProcessor dictionaryValidateProcessorEmail = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail(), db, this);
            dictionaryValidateProcessorEmail.setIdProfile(idProfile);
            rs.add(dictionaryValidateProcessorEmail);
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
                            response.setFailed((String) validatorResult.getName());
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

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                    Updates.set(DbKeyConfig.GENDER, request.getGender()),
                    Updates.set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                    Updates.set(DbKeyConfig.HOMETOWN, request.getHometown()),
                    Updates.set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                    Updates.set(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName()),
                    Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                    Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                    Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
                    Updates.set(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName()),
                    Updates.set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                    Updates.set(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName()),
                    Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName()),
                    Updates.set(DbKeyConfig.HR_REF, request.getHrRef()),
                    Updates.set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, parseVietnameseToEnglish(request.getFullName())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    Updates.set(DbKeyConfig.DEPARTMENT_ID, request.getDepartment()),
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName()),
                    Updates.set(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool())

            );
            // insert to rabbitmq
            ProfileRabbitMQEntity profileRabbitMQ = getProfileRabbit(request, dictionaryNames);
            publishActionToRabbitMQ(RabbitMQConfig.UPDATE, profileRabbitMQ);

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Cập nhật thông tin ứng viên", request.getInfo());

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
    public BaseResponse updateDetailProfile(UpdateDetailProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            if (!Strings.isNullOrEmpty(request.getSchool())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SCHOOL, request.getSchool(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB, request.getJob(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getDepartment())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.DEPARTMENT, request.getDepartment(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getLevelJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            }
            if (request.getSkill() != null && !request.getSkill().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_SKILL, request.getSkill(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, request.getId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail(), db, this));
            if (!Strings.isNullOrEmpty(request.getPhoneNumber())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorPhoneNumber = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber(), db, this);
                dictionaryValidateProcessorPhoneNumber.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorPhoneNumber);
            }
            DictionaryValidateProcessor dictionaryValidateProcessorEmail = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail(), db, this);
            dictionaryValidateProcessorEmail.setIdProfile(idProfile);
            rs.add(dictionaryValidateProcessorEmail);
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
                            response.setFailed((String) validatorResult.getName());
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

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);

            //Update coll calendar
            if (!dictionaryNames.getEmail().equals(request.getEmail())) {
                Bson id = Filters.eq(DbKeyConfig.ID_PROFILE, request.getId());
                Bson updateProfile = Updates.combine(
                        Updates.set(DbKeyConfig.EMAIL, request.getEmail())
                );
                db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, id, updateProfile, true);
            }

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                    Updates.set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                    Updates.set(DbKeyConfig.HOMETOWN, request.getHometown()),
                    Updates.set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                    Updates.set(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName()),
                    Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                    Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                    Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
                    Updates.set(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName()),
                    Updates.set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                    Updates.set(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName()),
                    Updates.set(DbKeyConfig.GENDER, request.getGender()),
                    Updates.set(DbKeyConfig.LAST_APPLY, request.getLastApply()),
                    Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                    Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName()),
                    Updates.set(DbKeyConfig.HR_REF, request.getHrRef()),
                    Updates.set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, parseVietnameseToEnglish(request.getFullName())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    Updates.set(DbKeyConfig.DEPARTMENT_ID, request.getDepartment()),
                    Updates.set(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName()),
                    Updates.set(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool())
            );
            // insert to rabbitmq
            ProfileRabbitMQEntity profileRabbitMQ = getProfileRabbit(request, dictionaryNames);
            publishActionToRabbitMQ(RabbitMQConfig.UPDATE_DETAIL, profileRabbitMQ);

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Cập nhật thông tin chi tiết ứng viên", request.getInfo());

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
    public BaseResponse deleteProfile(DeleteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            //Validate
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

            if (idDocument == null) {
                response.setFailed("Không tồn tại id profile này");
                return response;
            }

            deleteFile(AppUtils.parseString(idDocument.get(DbKeyConfig.URL_CV)));

            // insert to rabbitmq
            publishActionToRabbitMQ(RabbitMQConfig.DELETE, request);

            //Xóa profile
            db.delete(CollectionNameDefs.COLL_PROFILE, cond);

            //Xóa lịch phỏng vấn
            calendarService.deleteCalendarByIdProfile(request.getId());

            //Xóa note
            noteService.deleteNoteProfileByIdProfile(request.getId());

            //Xóa lịch sử
            historyService.deleteHistory(id);

            response.setSuccess();

            return response;
        } catch (Throwable e) {
            logger.info("Exception: ", e);
            response.setFailed("Hệ thống đang bận");
            return response;
        }
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getRecruitmentId(), db, this));
            DictionaryValidateProcessor dictionaryValidateProcessor = new DictionaryValidateProcessor(key, ThreadConfig.STATUS_CV, request.getStatusCV(), db, this);
            dictionaryValidateProcessor.setRecruitmentId(request.getRecruitmentId());
            rs.add(dictionaryValidateProcessor);
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
                            response.setFailed((String) validatorResult.getName());
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

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.STATUS_CV_ID, request.getStatusCV()),
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            response.setSuccess();

            // insert to rabbitmq
            ProfileRabbitMQEntity profileRabbitMQ = getProfileRabbit(request, dictionaryNames);
            publishActionToRabbitMQ(RabbitMQConfig.UPDATE_STATUS, profileRabbitMQ);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Chuyển vòng tuyển dụng", request.getInfo());

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
    public BaseResponse updateRejectProfile(UpdateRejectProfileRequest request) {


        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getIdProfile();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.REJECT_PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.REASON, request.getReason(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.RECRUITMENT, request.getRecruitmentId(), db, this));
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
                            response.setFailed((String) validatorResult.getName());
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

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.STATUS_CV_ID, ""),
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, "Loại"),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            Document recruitment = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.ID, request.getRecruitmentId()));
            List<Document> doc = (List<Document>) recruitment.get("interview_process");

            for (Document document1 : doc) {
                if (AppUtils.parseString(document1.get(DbKeyConfig.ID)).equals(dictionaryNames.getStatusCVId())) {
                    Bson cond2 = Filters.and(Filters.eq(DbKeyConfig.ID, request.getRecruitmentId()), Filters.eq("interview_process.id", dictionaryNames.getStatusCVId()));
                    Bson updateTotal = Updates.combine(
                            Updates.set("interview_process.$.total", AppUtils.parseLong(document1.get(DbKeyConfig.TOTAL)) - 1)
                    );
                    db.update(CollectionNameDefs.COLL_RECRUITMENT, cond2, updateTotal, true);
                    break;
                }
            }

            Bson reject = Updates.combine(
                    Updates.set(DbKeyConfig.ID_PROFILE, request.getIdProfile()),
                    Updates.set(DbKeyConfig.STATUS_CV_ID, dictionaryNames.getStatusCVId()),
                    Updates.set(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName()),
                    Updates.set(DbKeyConfig.RECRUITMENT_TIME, dictionaryNames.getRecruitmentTime()),
                    Updates.set(DbKeyConfig.REASON_ID, request.getReason()),
                    Updates.set(DbKeyConfig.REASON, dictionaryNames.getReason())
            );
            db.update(CollectionNameDefs.COLL_REASON_REJECT_PROFILE, Filters.eq(DbKeyConfig.ID_PROFILE, request.getIdProfile()), reject, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Loại ứng viên", request.getInfo());

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
    public BaseResponse updateTalentPoolProfile(UpdateTalentPoolProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getProfileId();

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL_PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPoolId(), db, this));
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
                            response.setFailed((String) validatorResult.getName());
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

            DictionaryNamesEntity dictionaryNames = getDictionayNames(rs);

            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.TALENT_POOL_ID, request.getTalentPoolId()),
                    Updates.set(DbKeyConfig.TALENT_POOL_NAME, dictionaryNames.getTalentPoolName()),
                    Updates.set(DbKeyConfig.TALENT_POOL_TIME, System.currentTimeMillis())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idProfile), updates, true);

//            Bson cond = Filters.and(Filters.eq(DbKeyConfig.ID, request.getProfileId()), Filters.eq(DbKeyConfig.TALENTPOOL_ID, request.getTalentPoolId()));
//            Document document = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
//            if (document == null) {
//                Document talentPool = new Document();
//                talentPool.append(DbKeyConfig.ID, request.getTalentPoolId());
//                talentPool.append(DbKeyConfig.TIME, System.currentTimeMillis());
//                Bson updates = Updates.combine(
//                        Updates.push(DbKeyConfig.TALENT_POOL, talentPool)
//                );
//                db.update(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idProfile), updates, true);
//            }

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Chuyển ứng viên vào talent pool", request.getInfo());

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
    public BaseResponse deleteTalentPoolProfile(DeleteTalentPoolProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getProfileId();
            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL_PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPoolId(), db, this));
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
                            response.setFailed((String) validatorResult.getName());
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

            Bson updates = Updates.combine(
                    Updates.pull(DbKeyConfig.TALENT_POOL, Filters.eq(DbKeyConfig.ID, request.getTalentPoolId()))
            );
            db.update(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, idProfile), updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Chuyển ứng viên vào talent pool", request.getInfo());

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
    public void isOld(String id) {
        try {
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.IS_NEW, false)
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            ProfileRabbitMQEntity profileRabbitMQ = new ProfileRabbitMQEntity();
            profileRabbitMQ.setId(id);
            profileRabbitMQ.setIsNew(false);
            publishActionToRabbitMQ(RabbitMQConfig.IS_OLD, profileRabbitMQ);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    private DictionaryNamesEntity getDictionayNames(List<DictionaryValidateProcessor> rs) {
        DictionaryNamesEntity dictionaryNames = new DictionaryNamesEntity();
        for (DictionaryValidateProcessor r : rs) {
            switch (r.getResult().getType()) {
                case ThreadConfig.JOB: {
                    dictionaryNames.setJobName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.REASON: {
                    dictionaryNames.setReason((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.JOB_LEVEL: {
                    dictionaryNames.setLevelJobName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.SCHOOL: {
                    dictionaryNames.setSchoolName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.SOURCE_CV: {
                    dictionaryNames.setSourceCVName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.TALENT_POOL: {
                    dictionaryNames.setTalentPoolName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.DEPARTMENT: {
                    dictionaryNames.setDepartmentName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.PROFILE: {
                    dictionaryNames.setEmail((String) r.getResult().getName());
                    dictionaryNames.setRecruitmentId(r.getResult().getIdProfile());
                    break;
                }
                case ThreadConfig.REJECT_PROFILE: {
                    dictionaryNames.setRecruitmentTime((Long) r.getResult().getName());
                    dictionaryNames.setStatusCVName(r.getResult().getFullName());
                    dictionaryNames.setStatusCVId(r.getResult().getStatusCVId());
                    break;
                }
                case ThreadConfig.STATUS_CV: {
                    dictionaryNames.setStatusCVName((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.LIST_SKILL: {
                    dictionaryNames.setSkill((List<Document>) r.getResult().getName());
                    break;
                }
                default: {
                    logger.info("Không có tên của dictionary này!");
                    break;
                }
            }
        }
        return dictionaryNames;
    }

    private ProfileRabbitMQEntity getProfileRabbit(UpdateStatusProfileRequest request, DictionaryNamesEntity dictionaryNames) {
        ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
        profileEntity.setId(request.getId());
        profileEntity.setStatusCVId(request.getStatusCV());
        profileEntity.setStatusCVName(dictionaryNames.getStatusCVName());
        return profileEntity;
    }

    private ProfileRabbitMQEntity getProfileRabbit(String id, CreateProfileRequest request, DictionaryNamesEntity dictionaryNames) {
        ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
        profileEntity.setId(id);
        profileEntity.setFullName(request.getFullName());
        profileEntity.setGender(request.getGender());
        profileEntity.setPhoneNumber(request.getPhoneNumber());
        profileEntity.setEmail(request.getEmail());
        profileEntity.setDateOfBirth(request.getDateOfBirth());
        profileEntity.setHometown(request.getHometown());
        profileEntity.setSchoolId(request.getSchool());
        profileEntity.setSchoolName(dictionaryNames.getSchoolName());
        profileEntity.setJobId(request.getJob());
        profileEntity.setJobName(dictionaryNames.getJobName());
        profileEntity.setLevelJobId(request.getLevelJob());
        profileEntity.setLevelJobName(dictionaryNames.getLevelJobName());
        profileEntity.setSourceCVId(request.getSourceCV());
        profileEntity.setSourceCVName(dictionaryNames.getSourceCVName());
        profileEntity.setHrRef(request.getHrRef());
        profileEntity.setDateOfApply(request.getDateOfApply());
        profileEntity.setTalentPoolId(request.getTalentPool());
        profileEntity.setTalentPoolName(dictionaryNames.getTalentPoolName());
        profileEntity.setDepartmentId(request.getDepartment());
        profileEntity.setDepartmentName(dictionaryNames.getDepartmentName());
        profileEntity.setLevelSchool(request.getLevelSchool());
        profileEntity.setMailRef(request.getMailRef());
        profileEntity.setSkill(request.getSkill());
        profileEntity.setAvatarColor(request.getAvatarColor());
        profileEntity.setIsNew(true);
        return profileEntity;
    }

    private ProfileRabbitMQEntity getProfileRabbit(UpdateProfileRequest request, DictionaryNamesEntity dictionaryNames) {
        ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
        profileEntity.setId(request.getId());
        profileEntity.setFullName(request.getFullName());
        profileEntity.setGender(request.getGender());
        profileEntity.setPhoneNumber(request.getPhoneNumber());
        profileEntity.setEmail(request.getEmail());
        profileEntity.setDateOfBirth(request.getDateOfBirth());
        profileEntity.setHometown(request.getHometown());
        profileEntity.setSchoolId(request.getSchool());
        profileEntity.setSchoolName(dictionaryNames.getSchoolName());
        profileEntity.setJobId(request.getJob());
        profileEntity.setJobName(dictionaryNames.getJobName());
        profileEntity.setLevelJobId(request.getLevelJob());
        profileEntity.setLevelJobName(dictionaryNames.getLevelJobName());
        profileEntity.setSourceCVId(request.getSourceCV());
        profileEntity.setSourceCVName(dictionaryNames.getSourceCVName());
        profileEntity.setHrRef(request.getHrRef());
        profileEntity.setDateOfApply(request.getDateOfApply());
        profileEntity.setDepartmentId(request.getDepartment());
        profileEntity.setDepartmentName(dictionaryNames.getDepartmentName());
        profileEntity.setLevelSchool(request.getLevelSchool());
        profileEntity.setMailRef(request.getMailRef());
        profileEntity.setSkill(request.getSkill());
        return profileEntity;
    }

    private ProfileRabbitMQEntity getProfileRabbit(UpdateDetailProfileRequest request, DictionaryNamesEntity dictionaryNames) {
        ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
        profileEntity.setId(request.getId());
        profileEntity.setFullName(request.getFullName());
        profileEntity.setGender(request.getGender());
        profileEntity.setPhoneNumber(request.getPhoneNumber());
        profileEntity.setEmail(request.getEmail());
        profileEntity.setDateOfBirth(request.getDateOfBirth());
        profileEntity.setHometown(request.getHometown());
        profileEntity.setSchoolId(request.getSchool());
        profileEntity.setSchoolName(dictionaryNames.getSchoolName());
        profileEntity.setJobId(request.getJob());
        profileEntity.setJobName(dictionaryNames.getJobName());
        profileEntity.setLevelJobId(request.getLevelJob());
        profileEntity.setLevelJobName(dictionaryNames.getLevelJobName());
        profileEntity.setSourceCVId(request.getSourceCV());
        profileEntity.setSourceCVName(dictionaryNames.getSourceCVName());
        profileEntity.setHrRef(request.getHrRef());
        profileEntity.setDateOfApply(request.getDateOfApply());
        profileEntity.setDepartmentId(request.getDepartment());
        profileEntity.setDepartmentName(dictionaryNames.getDepartmentName());
        profileEntity.setLevelSchool(request.getLevelSchool());
        profileEntity.setEvaluation(request.getEvaluation());
        profileEntity.setLastApply(request.getLastApply());
        profileEntity.setMailRef(request.getMailRef());
        profileEntity.setSkill(request.getSkill());
        return profileEntity;
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
