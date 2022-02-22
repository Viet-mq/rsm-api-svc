package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.*;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Updates.*;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService, IDictionaryValidator {

    private final HistoryService historyService;
    private final HistoryEmailService historyEmailService;
    private final Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();
    private final RabbitTemplate rabbitTemplate;
    private final CalendarService calendarService;
    private final NoteService noteService;
    private final CommentService commentService;
    private final RabbitMQOnlineActions rabbitMQOnlineActions;

    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;
    @Value("${mail.fileSize}")
    private Long fileSize;
    @Value("${cv.path}")
    private String cvPath;

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, HistoryEmailService historyEmailService, RabbitTemplate rabbitTemplate, CalendarService calendarService, NoteService noteService, CommentService commentService, RabbitMQOnlineActions rabbitMQOnlineActions) {
        super(db);
        this.historyService = historyService;
        this.historyEmailService = historyEmailService;
        this.rabbitTemplate = rabbitTemplate;
        this.calendarService = calendarService;
        this.noteService = noteService;
        this.commentService = commentService;
        this.rabbitMQOnlineActions = rabbitMQOnlineActions;
    }

    private void publishActionToRabbitMQ(String type, Object profile) {
        EventEntity event = new EventEntity(type, profile);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
        logger.info("=>publishActionToRabbitMQ type: {}, profile: {}", type, profile);
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info,
                                                   String fullName,
                                                   String talentPool,
                                                   String job,
                                                   String levelJob,
                                                   String department,
                                                   String recruitment,
                                                   String calendar,
                                                   String statusCV,
                                                   String key,
                                                   String tag,
                                                   Long from,
                                                   Long to,
                                                   Integer page,
                                                   Integer size) {

        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(fullName))));
        }
        if (!Strings.isNullOrEmpty(talentPool)) {
            c.add(Filters.eq(DbKeyConfig.TALENT_POOL_ID, talentPool));
        }
        if (!Strings.isNullOrEmpty(key) && key.equals("follow")) {
            c.add(Filters.eq(DbKeyConfig.FOLLOWERS, info.getUsername()));
        }
        if (from != null && from > 0) {
            c.add(Filters.gte(DbKeyConfig.DATE_OF_APPLY, from));
        }
        if (to != null && to > 0) {
            c.add(Filters.lte(DbKeyConfig.DATE_OF_APPLY, to));
        }
        if (!Strings.isNullOrEmpty(job)) {
            c.add(Filters.eq(DbKeyConfig.JOB_ID, job));
        }
        if (!Strings.isNullOrEmpty(tag)) {
            c.add(Filters.eq(DbKeyConfig.TAGS, tag));
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
                        .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                        .skill((List<SkillEntity>) doc.get(DbKeyConfig.SKILL))
                        .avatarColor(AppUtils.parseString(doc.get(DbKeyConfig.AVATAR_COLOR)))
                        .isNew(AppUtils.parseString(doc.get(DbKeyConfig.IS_NEW)))
                        .followers((List<String>) doc.get(DbKeyConfig.FOLLOWERS))
                        .tags((List<String>) doc.get(DbKeyConfig.TAGS))
                        .picId(AppUtils.parseString(doc.get(DbKeyConfig.PIC_ID)))
                        .picName(AppUtils.parseString(doc.get(DbKeyConfig.PIC_NAME)))
                        .picMail(AppUtils.parseString(doc.get(DbKeyConfig.PIC_MAIL)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .linkedin(AppUtils.parseString(doc.get(DbKeyConfig.LINKEDIN)))
                        .facebook(AppUtils.parseString(doc.get(DbKeyConfig.FACEBOOK)))
                        .skype(AppUtils.parseString(doc.get(DbKeyConfig.SKYPE)))
                        .github(AppUtils.parseString(doc.get(DbKeyConfig.GITHUB)))
                        .otherTech(AppUtils.parseString(doc.get(DbKeyConfig.OTHER_TECH)))
                        .web(AppUtils.parseString(doc.get(DbKeyConfig.WEB)))
                        .status(AppUtils.parseString(doc.get(DbKeyConfig.STATUS)))
                        .companyId(AppUtils.parseString(doc.get(DbKeyConfig.COMPANY_ID)))
                        .companyName(AppUtils.parseString(doc.get(DbKeyConfig.COMPANY_NAME)))
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
                .username(AppUtils.parseString(one.get(DbKeyConfig.USERNAME)))
                .skill((List<SkillEntity>) one.get(DbKeyConfig.SKILL))
                .avatarColor(AppUtils.parseString(one.get(DbKeyConfig.AVATAR_COLOR)))
                .followers((List<String>) one.get(DbKeyConfig.FOLLOWERS))
                .tags((List<String>) one.get(DbKeyConfig.TAGS))
                .picId(AppUtils.parseString(one.get(DbKeyConfig.PIC_ID)))
                .picName(AppUtils.parseString(one.get(DbKeyConfig.PIC_NAME)))
                .picMail(AppUtils.parseString(one.get(DbKeyConfig.PIC_MAIL)))
                .time(AppUtils.parseLong(one.get(DbKeyConfig.TIME)))
                .linkedin(AppUtils.parseString(one.get(DbKeyConfig.LINKEDIN)))
                .facebook(AppUtils.parseString(one.get(DbKeyConfig.FACEBOOK)))
                .skype(AppUtils.parseString(one.get(DbKeyConfig.SKYPE)))
                .github(AppUtils.parseString(one.get(DbKeyConfig.GITHUB)))
                .otherTech(AppUtils.parseString(one.get(DbKeyConfig.OTHER_TECH)))
                .web(AppUtils.parseString(one.get(DbKeyConfig.WEB)))
                .status(AppUtils.parseString(one.get(DbKeyConfig.STATUS)))
                .companyId(AppUtils.parseString(one.get(DbKeyConfig.COMPANY_ID)))
                .companyName(AppUtils.parseString(one.get(DbKeyConfig.COMPANY_NAME)))
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
            if (!Strings.isNullOrEmpty(request.getHrRef())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getHrRef(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getPic())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PIC, request.getPic(), db, this));
            }
            if (request.getSkill() != null && !request.getSkill().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_SKILL, request.getSkill(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getCompany())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.COMPANY, request.getCompany(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            if (!Strings.isNullOrEmpty(request.getPhoneNumber())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber().trim(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorPhoneNumber = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber().trim(), db, this);
                dictionaryValidateProcessorPhoneNumber.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorPhoneNumber);
            }
            if (!Strings.isNullOrEmpty(request.getEmail())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail().trim(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorEmail = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail().trim(), db, this);
                dictionaryValidateProcessorEmail.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorEmail);
            }
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
            profile.append(DbKeyConfig.FULL_NAME, AppUtils.mergeWhitespace(request.getFullName()));
            profile.append(DbKeyConfig.GENDER, request.getGender());
            profile.append(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber().replaceAll(" ", ""));
            profile.append(DbKeyConfig.EMAIL, request.getEmail().replaceAll(" ", ""));
            profile.append(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth());
            profile.append(DbKeyConfig.HOMETOWN, AppUtils.mergeWhitespace(request.getHometown()));
            profile.append(DbKeyConfig.SCHOOL_ID, request.getSchool());
            profile.append(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName());
            profile.append(DbKeyConfig.JOB_ID, request.getJob());
            profile.append(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName());
            profile.append(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob());
            profile.append(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName());
            profile.append(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV());
            profile.append(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName());
            profile.append(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply());
            profile.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getFullName()));
            profile.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            profile.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            profile.append(DbKeyConfig.DEPARTMENT_ID, request.getDepartment());
            profile.append(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName());
            profile.append(DbKeyConfig.LEVEL_SCHOOL, AppUtils.mergeWhitespace(request.getLevelSchool()));
            profile.append(DbKeyConfig.USERNAME, request.getHrRef());
            profile.append(DbKeyConfig.HR_REF, dictionaryNames.getFullNameUser());
            profile.append(DbKeyConfig.MAIL_REF, dictionaryNames.getEmailUser());
            profile.append(DbKeyConfig.SKILL, dictionaryNames.getSkill());
            profile.append(DbKeyConfig.AVATAR_COLOR, request.getAvatarColor());
            profile.append(DbKeyConfig.IS_NEW, true);
            profile.append(DbKeyConfig.PIC_ID, request.getPic());
            profile.append(DbKeyConfig.PIC_NAME, dictionaryNames.getFullNamePIC());
            profile.append(DbKeyConfig.PIC_MAIL, dictionaryNames.getPicEmail());
            profile.append(DbKeyConfig.TIME, request.getTime());
            profile.append(DbKeyConfig.LINKEDIN, request.getLinkedin());
            profile.append(DbKeyConfig.FACEBOOK, request.getFacebook());
            profile.append(DbKeyConfig.SKYPE, request.getSkype());
            profile.append(DbKeyConfig.GITHUB, request.getGithub());
            profile.append(DbKeyConfig.OTHER_TECH, request.getOtherTech());
            profile.append(DbKeyConfig.WEB, request.getWeb());
            profile.append(DbKeyConfig.STATUS, request.getStatus());
            profile.append(DbKeyConfig.COMPANY_ID, request.getCompany());
            profile.append(DbKeyConfig.COMPANY_NAME, dictionaryNames.getCompanyName());

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
            if (!Strings.isNullOrEmpty(request.getHrRef())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getHrRef(), db, this));
            }
            if (request.getSkill() != null && !request.getSkill().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_SKILL, request.getSkill(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getPic())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PIC, request.getPic(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getCompany())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.COMPANY, request.getCompany(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, request.getId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            if (!Strings.isNullOrEmpty(request.getPhoneNumber())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber().trim(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorPhoneNumber = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber().trim(), db, this);
                dictionaryValidateProcessorPhoneNumber.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorPhoneNumber);
            }
            if (!Strings.isNullOrEmpty(request.getEmail())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail().trim(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorEmail = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail().trim(), db, this);
                dictionaryValidateProcessorEmail.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorEmail);
            }
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
                    set(DbKeyConfig.FULL_NAME, AppUtils.mergeWhitespace(request.getFullName())),
                    set(DbKeyConfig.GENDER, request.getGender()),
                    set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                    set(DbKeyConfig.HOMETOWN, AppUtils.mergeWhitespace(request.getHometown())),
                    set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                    set(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName()),
                    set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber().replaceAll(" ", "")),
                    set(DbKeyConfig.EMAIL, request.getEmail().replaceAll(" ", "")),
                    set(DbKeyConfig.JOB_ID, request.getJob()),
                    set(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName()),
                    set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                    set(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName()),
                    set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    set(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName()),
                    set(DbKeyConfig.USERNAME, request.getHrRef()),
                    set(DbKeyConfig.HR_REF, dictionaryNames.getFullNameUser()),
                    set(DbKeyConfig.MAIL_REF, dictionaryNames.getEmailUser()),
                    set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getFullName())),
                    set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    set(DbKeyConfig.DEPARTMENT_ID, request.getDepartment()),
                    set(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName()),
                    set(DbKeyConfig.LEVEL_SCHOOL, AppUtils.mergeWhitespace(request.getLevelSchool())),
                    set(DbKeyConfig.SKILL, dictionaryNames.getSkill()),
                    set(DbKeyConfig.PIC_ID, request.getPic()),
                    set(DbKeyConfig.PIC_NAME, dictionaryNames.getFullNamePIC()),
                    set(DbKeyConfig.PIC_MAIL, dictionaryNames.getPicEmail()),
                    set(DbKeyConfig.TIME, request.getTime()),
                    set(DbKeyConfig.LINKEDIN, request.getLinkedin()),
                    set(DbKeyConfig.FACEBOOK, request.getFacebook()),
                    set(DbKeyConfig.SKYPE, request.getSkype()),
                    set(DbKeyConfig.GITHUB, request.getGithub()),
                    set(DbKeyConfig.OTHER_TECH, request.getOtherTech()),
                    set(DbKeyConfig.WEB, request.getWeb()),
                    set(DbKeyConfig.STATUS, request.getStatus()),
                    set(DbKeyConfig.COMPANY_ID, request.getCompany()),
                    set(DbKeyConfig.COMPANY_NAME, dictionaryNames.getCompanyName())
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
            if (!Strings.isNullOrEmpty(request.getHrRef())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getHrRef(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getLevelJob())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
            }
            if (request.getSkill() != null && !request.getSkill().isEmpty()) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_SKILL, request.getSkill(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getPic())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PIC, request.getPic(), db, this));
            }
            if (!Strings.isNullOrEmpty(request.getCompany())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.COMPANY, request.getCompany(), db, this));
            }
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, request.getId(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.SOURCE_CV, request.getSourceCV(), db, this));
            if (!Strings.isNullOrEmpty(request.getPhoneNumber())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, request.getPhoneNumber().trim(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorPhoneNumber = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, request.getPhoneNumber().trim(), db, this);
                dictionaryValidateProcessorPhoneNumber.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorPhoneNumber);
            }
            if (!Strings.isNullOrEmpty(request.getEmail())) {
                rs.add(new DictionaryValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, request.getEmail().trim(), db, this));
                DictionaryValidateProcessor dictionaryValidateProcessorEmail = new DictionaryValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, request.getEmail().trim(), db, this);
                dictionaryValidateProcessorEmail.setIdProfile(idProfile);
                rs.add(dictionaryValidateProcessorEmail);
            }
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
                        set(DbKeyConfig.EMAIL, request.getEmail())
                );
                db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, id, updateProfile, true);
            }

            Bson updates = Updates.combine(
                    set(DbKeyConfig.FULL_NAME, AppUtils.mergeWhitespace(request.getFullName())),
                    set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                    set(DbKeyConfig.HOMETOWN, AppUtils.mergeWhitespace(request.getHometown())),
                    set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                    set(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName()),
                    set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber().replaceAll(" ", "")),
                    set(DbKeyConfig.EMAIL, request.getEmail().replaceAll(" ", "")),
                    set(DbKeyConfig.JOB_ID, request.getJob()),
                    set(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName()),
                    set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                    set(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName()),
                    set(DbKeyConfig.GENDER, request.getGender()),
                    set(DbKeyConfig.LAST_APPLY, request.getLastApply()),
                    set(DbKeyConfig.EVALUATION, AppUtils.mergeWhitespace(request.getEvaluation())),
                    set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                    set(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName()),
                    set(DbKeyConfig.USERNAME, request.getHrRef()),
                    set(DbKeyConfig.HR_REF, dictionaryNames.getFullNameUser()),
                    set(DbKeyConfig.MAIL_REF, dictionaryNames.getEmailUser()),
                    set(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply()),
                    set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(request.getFullName())),
                    set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername()),
                    set(DbKeyConfig.DEPARTMENT_ID, request.getDepartment()),
                    set(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName()),
                    set(DbKeyConfig.LEVEL_SCHOOL, AppUtils.mergeWhitespace(request.getLevelSchool())),
                    set(DbKeyConfig.SKILL, dictionaryNames.getSkill()),
                    set(DbKeyConfig.PIC_ID, request.getPic()),
                    set(DbKeyConfig.PIC_NAME, dictionaryNames.getFullNamePIC()),
                    set(DbKeyConfig.PIC_MAIL, dictionaryNames.getPicEmail()),
                    set(DbKeyConfig.TIME, request.getTime()),
                    set(DbKeyConfig.LINKEDIN, request.getLinkedin()),
                    set(DbKeyConfig.FACEBOOK, request.getFacebook()),
                    set(DbKeyConfig.SKYPE, request.getSkype()),
                    set(DbKeyConfig.GITHUB, request.getGithub()),
                    set(DbKeyConfig.OTHER_TECH, request.getOtherTech()),
                    set(DbKeyConfig.WEB, request.getWeb()),
                    set(DbKeyConfig.STATUS, request.getStatus()),
                    set(DbKeyConfig.COMPANY_ID, request.getCompany()),
                    set(DbKeyConfig.COMPANY_NAME, dictionaryNames.getCompanyName())
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

            String cv = AppUtils.parseString(idDocument.get(DbKeyConfig.CV));
            if (!Strings.isNullOrEmpty(cv)) {
                deleteFile(cvPath + cv);
            }

            // insert to rabbitmq
            publishActionToRabbitMQ(RabbitMQConfig.DELETE, request);

            //Xóa profile
            db.delete(CollectionNameDefs.COLL_PROFILE, cond);

            //Xóa lịch phỏng vấn
            calendarService.deleteCalendarByIdProfile(id);

            //Xóa note
            noteService.deleteNoteProfileByIdProfile(id);

            //Xóa lịch sử
            historyService.deleteHistory(id);
            historyEmailService.deleteHistoryEmail(id);

            //Xóa comment
            commentService.deleteCommentProfileByIdProfile(id);

            //-1 in recruitment
            if (!Strings.isNullOrEmpty(AppUtils.parseString(idDocument.get(DbKeyConfig.RECRUITMENT_ID)))) {
                Document recruitment = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.ID, idDocument.get(DbKeyConfig.RECRUITMENT_ID)));
                if (recruitment != null) {
                    List<Document> doc = (List<Document>) recruitment.get(DbKeyConfig.INTERVIEW_PROCESS);
                    for (Document document : doc) {
                        if (AppUtils.parseString(document.get(DbKeyConfig.ID)).equals(AppUtils.parseString(idDocument.get(DbKeyConfig.STATUS_CV_ID)))) {
                            Bson con = Filters.and(Filters.eq(DbKeyConfig.ID, idDocument.get(DbKeyConfig.RECRUITMENT_ID)), Filters.eq("interview_process.id", idDocument.get(DbKeyConfig.STATUS_CV_ID)));
                            Bson updateTotal = Updates.combine(
                                    Updates.set("interview_process.$.total", AppUtils.parseLong(document.get(DbKeyConfig.TOTAL)) - 1)
                            );
                            db.update(CollectionNameDefs.COLL_RECRUITMENT, con, updateTotal, true);
                            break;
                        }
                    }
                }
            }

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
                    set(DbKeyConfig.STATUS_CV_ID, request.getStatusCV()),
                    set(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName()),
                    set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
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
    public BaseResponse updateRejectProfile(UpdateRejectProfileRequest request, CandidateRequest candidate, PresenterRequest presenter) {

        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            if (presenter.getFilePresenters() != null && !presenter.getFilePresenters().isEmpty()) {
                for (MultipartFile file : presenter.getFilePresenters()) {
                    if (file != null && file.getSize() > fileSize) {
                        return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
                    }
                }
            }
            if (candidate.getFileCandidates() != null && !candidate.getFileCandidates().isEmpty()) {
                for (MultipartFile file : candidate.getFileCandidates()) {
                    if (file != null && file.getSize() > fileSize) {
                        return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
                    }
                }
            }
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
                    set(DbKeyConfig.STATUS_CV_ID, "d0d08b54-4ac7-4947-9018-e8c4f991b7bs"),
                    set(DbKeyConfig.STATUS_CV_NAME, "Loại"),
                    set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                    set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            Document recruitment = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.ID, request.getRecruitmentId()));
            List<Document> doc = (List<Document>) recruitment.get("interview_process");

            for (Document document1 : doc) {
                if (AppUtils.parseString(document1.get(DbKeyConfig.ID)).equals(dictionaryNames.getStatusCVId())) {
                    Bson cond2 = Filters.and(Filters.eq(DbKeyConfig.ID, request.getRecruitmentId()), Filters.eq("interview_process.id", dictionaryNames.getStatusCVId()));
                    Bson updateTotal = Updates.combine(
                            set("interview_process.$.total", AppUtils.parseLong(document1.get(DbKeyConfig.TOTAL)) - 1)
                    );
                    db.update(CollectionNameDefs.COLL_RECRUITMENT, cond2, updateTotal, true);
                    break;
                }
            }

            Bson reject = Updates.combine(
                    set(DbKeyConfig.ID_PROFILE, request.getIdProfile()),
                    set(DbKeyConfig.STATUS_CV_ID, dictionaryNames.getStatusCVId()),
                    set(DbKeyConfig.STATUS_CV_NAME, dictionaryNames.getStatusCVName()),
                    set(DbKeyConfig.RECRUITMENT_TIME, dictionaryNames.getRecruitmentTime()),
                    set(DbKeyConfig.REASON_ID, request.getReason()),
                    set(DbKeyConfig.REASON, dictionaryNames.getReason())
            );
            db.update(CollectionNameDefs.COLL_REASON_REJECT_PROFILE, Filters.eq(DbKeyConfig.ID_PROFILE, request.getIdProfile()), reject, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Loại ứng viên", request.getInfo());

            //publish rabbit
            if (!Strings.isNullOrEmpty(candidate.getSubjectCandidate()) && !Strings.isNullOrEmpty(candidate.getContentCandidate())) {
                String historyId = UUID.randomUUID().toString();
                List<String> paths = historyEmailService.createHistoryEmail(historyId, idProfile, candidate.getSubjectCandidate(), candidate.getContentCandidate(), candidate.getFileCandidates(), request.getInfo());
                rabbitMQOnlineActions.publishCandidateEmail(TypeConfig.REJECT_CANDIDATE, candidate, paths, historyId, request.getIdProfile());
            }
            if (!Strings.isNullOrEmpty(presenter.getSubjectPresenter()) && !Strings.isNullOrEmpty(presenter.getContentPresenter())) {
                String historyId = UUID.randomUUID().toString();
                List<String> paths = historyEmailService.createHistoryEmail(historyId, idProfile, presenter.getSubjectPresenter(), presenter.getContentPresenter(), presenter.getFilePresenters(), request.getInfo());
                rabbitMQOnlineActions.publishPresenterEmail(TypeConfig.REJECT_PRESENTER, presenter, paths, historyId, request.getIdProfile());
            }

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
                    set(DbKeyConfig.TALENT_POOL_ID, request.getTalentPoolId()),
                    set(DbKeyConfig.TALENT_POOL_NAME, dictionaryNames.getTalentPoolName()),
                    set(DbKeyConfig.TALENT_POOL_TIME, System.currentTimeMillis())
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
            Bson cond = Filters.eq(DbKeyConfig.ID, request.getProfileId());
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
            if (profile == null) {
                response.setFailed("Không tồn tại id profile này!");
                return response;
            }

            Bson updates = Updates.combine(
                    set(DbKeyConfig.TALENT_POOL_ID, ""),
                    set(DbKeyConfig.TALENT_POOL_NAME, "")
            );
            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(request.getProfileId(), TypeConfig.UPDATE, "Xóa ứng viên khỏi talent pool", request.getInfo());

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
                    set(DbKeyConfig.IS_NEW, false)
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

    @Override
    public BaseResponse mergeDuplicateProfile(MergeProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getIdProfile();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.MERGE_PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.MERGE_OTHER_PROFILE, request.getOtherIdProfile(), db, this));
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

            Document profile = getDictionayNames(rs).getProfile();
            Document otherProfile = getDictionayNames(rs).getOtherProfile();

            if (!AppUtils.parseString(profile.get(DbKeyConfig.EMAIL)).equals(AppUtils.parseString(otherProfile.get(DbKeyConfig.EMAIL))) ||
                    !Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.PHONE_NUMBER))) && !AppUtils.parseString(profile.get(DbKeyConfig.PHONE_NUMBER)).equals(AppUtils.parseString(profile.get(DbKeyConfig.PHONE_NUMBER)))) {
                response.setFailed("Vui lòng nhập đúng profile");
                return response;
            }

            List<Bson> update = new ArrayList<>();

            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.DATE_OF_BIRTH)))) {
                update.add(set(DbKeyConfig.DATE_OF_BIRTH, otherProfile.get(DbKeyConfig.DATE_OF_BIRTH)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.HOMETOWN)))) {
                update.add(set(DbKeyConfig.HOMETOWN, otherProfile.get(DbKeyConfig.HOMETOWN)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.SCHOOL_ID)))) {
                update.add(set(DbKeyConfig.SCHOOL_ID, otherProfile.get(DbKeyConfig.SCHOOL_ID)));
                update.add(set(DbKeyConfig.SCHOOL_NAME, otherProfile.get(DbKeyConfig.SCHOOL_NAME)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.PHONE_NUMBER)))) {
                update.add(set(DbKeyConfig.PHONE_NUMBER, otherProfile.get(DbKeyConfig.PHONE_NUMBER)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.LEVEL_JOB_ID)))) {
                update.add(set(DbKeyConfig.LEVEL_JOB_ID, otherProfile.get(DbKeyConfig.LEVEL_JOB_ID)));
                update.add(set(DbKeyConfig.LEVEL_JOB_NAME, otherProfile.get(DbKeyConfig.LEVEL_JOB_NAME)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.GENDER)))) {
                update.add(set(DbKeyConfig.GENDER, otherProfile.get(DbKeyConfig.GENDER)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.LAST_APPLY)))) {
                update.add(set(DbKeyConfig.LAST_APPLY, otherProfile.get(DbKeyConfig.LAST_APPLY)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.EVALUATION)))) {
                update.add(set(DbKeyConfig.EVALUATION, otherProfile.get(DbKeyConfig.EVALUATION)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.USERNAME)))) {
                update.add(set(DbKeyConfig.USERNAME, otherProfile.get(DbKeyConfig.USERNAME)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.HR_REF)))) {
                update.add(set(DbKeyConfig.HR_REF, otherProfile.get(DbKeyConfig.HR_REF)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.MAIL_REF)))) {
                update.add(set(DbKeyConfig.MAIL_REF, otherProfile.get(DbKeyConfig.MAIL_REF)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.DEPARTMENT_ID)))) {
                update.add(set(DbKeyConfig.DEPARTMENT_ID, otherProfile.get(DbKeyConfig.DEPARTMENT_ID)));
                update.add(set(DbKeyConfig.DEPARTMENT_NAME, otherProfile.get(DbKeyConfig.DEPARTMENT_NAME)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.LEVEL_SCHOOL)))) {
                update.add(set(DbKeyConfig.LEVEL_SCHOOL, otherProfile.get(DbKeyConfig.LEVEL_SCHOOL)));
            }
            if (Strings.isNullOrEmpty(AppUtils.parseString(profile.get(DbKeyConfig.SKILL)))) {
                update.add(set(DbKeyConfig.SKILL, otherProfile.get(DbKeyConfig.SKILL)));
            }

            Bson updates = Updates.combine(update);

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
            db.delete(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.ID, request.getOtherIdProfile()));

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Gộp trùng ứng viên", request.getInfo());

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
    public BaseResponse addFollower(AddFollowerRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getProfileId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getUsername(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.FOLLOWER, request.getUsername(), db, this));
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
                    push(DbKeyConfig.FOLLOWERS, request.getUsername())
            );

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Thêm người theo dõi ứng viên", request.getInfo());

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
    public BaseResponse deleteFollower(DeleteFollowerRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getProfileId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.USER, request.getUsername(), db, this));
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
                    pull(DbKeyConfig.FOLLOWERS, request.getUsername())
            );

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Thêm người theo dõi ứng viên", request.getInfo());

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
    public BaseResponse addTags(AddTagsProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getProfileId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.LIST_TAG, request.getTags(), db, this));
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

            Document profile = getDictionayNames(rs).getDocument();
            List<String> list = (List<String>) profile.get(DbKeyConfig.TAGS);
            Set<String> tags = new HashSet<>();
            if (list != null) {
                tags.addAll(list);
            }
            tags.addAll(request.getTags());

            Bson updates = Updates.combine(
                    set(DbKeyConfig.TAGS, tags)
            );

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Thêm thẻ cho ứng viên", request.getInfo());

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
    public BaseResponse deleteTag(DeleteTagProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String key = UUID.randomUUID().toString();

        try {
            //Validate
            String idProfile = request.getProfileId();
            Bson cond = Filters.eq(DbKeyConfig.ID, idProfile);

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.PROFILE, idProfile, db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TAG, request.getTag(), db, this));
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
                    pull(DbKeyConfig.TAGS, request.getTag())
            );

            db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

            //Insert history to DB
            historyService.createHistory(idProfile, TypeConfig.UPDATE, "Xóa thẻ của ứng viên", request.getInfo());

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
                case ThreadConfig.USER: {
                    dictionaryNames.setEmailUser((String) r.getResult().getName());
                    dictionaryNames.setFullNameUser(r.getResult().getFullName());
                    break;
                }
                case ThreadConfig.PIC: {
                    dictionaryNames.setFullNamePIC((String) r.getResult().getName());
                    dictionaryNames.setPicEmail(r.getResult().getMailRef());
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
                    dictionaryNames.setDocument(r.getResult().getDocument());
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
                case ThreadConfig.PROFILE_EMAIL: {
                    dictionaryNames.setProfileEmail((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.PROFILE_PHONE_NUMBER: {
                    dictionaryNames.setProfilePhoneNumber((String) r.getResult().getName());
                    break;
                }
                case ThreadConfig.MERGE_OTHER_PROFILE: {
                    dictionaryNames.setOtherProfile((Document) r.getResult().getName());
                    break;
                }
                case ThreadConfig.MERGE_PROFILE: {
                    dictionaryNames.setProfile((Document) r.getResult().getName());
                    break;
                }
                case ThreadConfig.COMPANY: {
                    dictionaryNames.setCompanyName((String) r.getResult().getName());
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
        profileEntity.setHrRef(dictionaryNames.getFullNameUser());
        profileEntity.setDateOfApply(request.getDateOfApply());
        profileEntity.setTalentPoolId(request.getTalentPool());
        profileEntity.setTalentPoolName(dictionaryNames.getTalentPoolName());
        profileEntity.setDepartmentId(request.getDepartment());
        profileEntity.setDepartmentName(dictionaryNames.getDepartmentName());
        profileEntity.setLevelSchool(request.getLevelSchool());
        profileEntity.setMailRef(dictionaryNames.getEmailUser());
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
        profileEntity.setHrRef(dictionaryNames.getFullNameUser());
        profileEntity.setDateOfApply(request.getDateOfApply());
        profileEntity.setDepartmentId(request.getDepartment());
        profileEntity.setDepartmentName(dictionaryNames.getDepartmentName());
        profileEntity.setLevelSchool(request.getLevelSchool());
        profileEntity.setMailRef(dictionaryNames.getEmailUser());
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
        profileEntity.setHrRef(dictionaryNames.getFullNameUser());
        profileEntity.setDateOfApply(request.getDateOfApply());
        profileEntity.setDepartmentId(request.getDepartment());
        profileEntity.setDepartmentName(dictionaryNames.getDepartmentName());
        profileEntity.setLevelSchool(request.getLevelSchool());
        profileEntity.setEvaluation(request.getEvaluation());
        profileEntity.setLastApply(request.getLastApply());
        profileEntity.setMailRef(dictionaryNames.getEmailUser());
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
