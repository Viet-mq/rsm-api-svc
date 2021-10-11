package com.edso.resume.api.service;

import com.edso.resume.api.domain.Thread.*;
import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    private final MongoDbOnlineSyncActions db;
    private final HistoryService historyService;
    private final ValidateChecker validateChecker;
    private final BaseResponse response = new BaseResponse();

    public ProfileServiceImpl(MongoDbOnlineSyncActions db, HistoryService historyService, RabbitTemplate rabbitTemplate) {
        super(db, rabbitTemplate);
        this.db = db;
        this.historyService = historyService;
        validateChecker = new ValidateChecker(db);
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
        if (!validateDictionary(idProfile, CollectionNameDefs.COLL_PROFILE)) {
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
        historyService.createHistory(idProfile, "Xem chi tiết profile", info.getFullName());

        return response;
    }

    @Override
    public BaseResponse createProfile(CreateProfileRequest request) {

        String idProfile = UUID.randomUUID().toString();


        //Validate
        Document job = db.findOne(CollectionNameDefs.COLL_JOB, Filters.eq(DbKeyConfig.ID, request.getJob()));
        if (job == null) {
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        Document levelJob = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, Filters.eq(DbKeyConfig.ID, request.getLevelJob()));
        if (levelJob == null) {
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        Document school = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq(DbKeyConfig.ID, request.getSchool()));
        if (school == null) {
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        Document sourceCV = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, Filters.eq(DbKeyConfig.ID, request.getSourceCV()));
        if (sourceCV == null) {
            response.setFailed("Nguồn cv không tồn tại");
            return response;
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
        profile.append(DbKeyConfig.SCHOOL_NAME, school.get(DbKeyConfig.NAME));
        profile.append(DbKeyConfig.JOB_ID, request.getJob());
        profile.append(DbKeyConfig.JOB_NAME, job.get(DbKeyConfig.NAME));
        profile.append(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob());
        profile.append(DbKeyConfig.LEVEL_JOB_NAME, levelJob.get(DbKeyConfig.NAME));
        profile.append(DbKeyConfig.CV, request.getCv());
        profile.append(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV());
        profile.append(DbKeyConfig.SOURCE_CV_NAME, sourceCV.get(DbKeyConfig.NAME));
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

        // insert to rabbitmq
        insertToRabbitMQ("create profile", profile);

        //Insert history to DB
        historyService.createHistory(idProfile, "Tạo profile", request.getInfo().getFullName());

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse updateProfile(UpdateProfileRequest request) {

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        Document job = db.findOne(CollectionNameDefs.COLL_JOB, Filters.eq(DbKeyConfig.ID, request.getJob()));
        if (job == null) {
            response.setFailed("Công việc không tồn tại");
            return response;
        }

        Document levelJob = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, Filters.eq(DbKeyConfig.ID, request.getLevelJob()));
        if (levelJob == null) {
            response.setFailed("Vị trí tuyển dụng không tồn tại");
            return response;
        }

        Document school = db.findOne(CollectionNameDefs.COLL_SCHOOL, Filters.eq(DbKeyConfig.ID, request.getSchool()));
        if (school == null) {
            response.setFailed("Trường học không tồn tại");
            return response;
        }

        Document sourceCV = db.findOne(CollectionNameDefs.COLL_SOURCE_CV, Filters.eq(DbKeyConfig.ID, request.getSourceCV()));
        if (sourceCV == null) {
            response.setFailed("Nguồn cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                Updates.set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                Updates.set(DbKeyConfig.HOMETOWN, request.getHometown()),
                Updates.set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
                Updates.set(DbKeyConfig.SCHOOL_NAME, school.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
                Updates.set(DbKeyConfig.JOB_NAME, job.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
                Updates.set(DbKeyConfig.LEVEL_JOB_NAME, levelJob.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.CV, request.getCv()),
                Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
                Updates.set(DbKeyConfig.SOURCE_CV_NAME, sourceCV.get(DbKeyConfig.NAME)),
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

        // insert to rabbitmq
        insertToRabbitMQ("update profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Sửa profile", request.getInfo().getFullName());

        return response;

    }

    @Override
    public BaseResponse updateDetailProfile(UpdateDetailProfileRequest request) {

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        boolean check = validateChecker.validate(request.getLevelJob(), request.getJob(), request.getSchool(), request.getSourceCV());
        if(check){
            response.setFailed("Vui lòng chọn tất cả các mục");
            return response;
        }

//        final CountDownLatch latch = new CountDownLatch(4);
//        JobThread job = new JobThread(db, request.getJob(), latch);
//        JobLevelThread jobLevel = new JobLevelThread(db, request.getLevelJob(), latch);
//        SchoolThread school = new SchoolThread(db, request.getSchool(), latch);
//        SourceCVThread sourceCV = new SourceCVThread(db, request.getSourceCV(), latch);
//
//        List<ICountValidate> arr = new ArrayList<>();
//        arr.add(job);
//        arr.add(jobLevel);
//        arr.add(school);
//        arr.add(sourceCV);
//
//        new Thread(job).start();
//        new Thread(jobLevel).start();
//        new Thread(school).start();
//        new Thread(sourceCV).start();
//
//        // wait
//        try {
//            latch.await();
//        } catch (Throwable ex) {
//            ex.printStackTrace();
//        }
//
//        // kiem tra ket qua
//        for (ICountValidate processor : arr) {
//            if (processor.count() != null) {
//                response.setFailed(processor.count());
//                return response;
//            }
//        }
        // update roles
        Bson updates = Updates.combine(
                Updates.set(DbKeyConfig.FULL_NAME, request.getFullName()),
                Updates.set(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth()),
                Updates.set(DbKeyConfig.HOMETOWN, request.getHometown()),
                Updates.set(DbKeyConfig.SCHOOL_ID, request.getSchool()),
//                    Updates.set(DbKeyConfig.SCHOOL_NAME, school.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber()),
                Updates.set(DbKeyConfig.EMAIL, request.getEmail()),
                Updates.set(DbKeyConfig.JOB_ID, request.getJob()),
//                    Updates.set(DbKeyConfig.JOB_NAME, job.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob()),
//                    Updates.set(DbKeyConfig.LEVEL_JOB_NAME, levelJob.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.CV, request.getCv()),
                Updates.set(DbKeyConfig.TAGS, request.getTags()),
                Updates.set(DbKeyConfig.NOTE, request.getNote()),
                Updates.set(DbKeyConfig.GENDER, request.getGender()),
                Updates.set(DbKeyConfig.LAST_APPLY, request.getLastApply()),
                Updates.set(DbKeyConfig.EVALUATION, request.getEvaluation()),
                Updates.set(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV()),
//                    Updates.set(DbKeyConfig.SOURCE_CV_NAME, sourceCV.get(DbKeyConfig.NAME)),
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

        // insert to rabbitmq
        insertToRabbitMQ("update detail profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Sửa chi tiết profile", request.getInfo().getFullName());

        return response;
    }

    @Override
    public BaseResponse deleteProfile(DeleteProfileRequest request) {
        //Validate
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        // insert to rabbitmq
        insertToRabbitMQ("delete profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Xóa profile", request.getInfo().getFullName());
        response.setSuccess();

        return response;
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {

        //Validate
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);

        if (!validateDictionary(id, CollectionNameDefs.COLL_PROFILE)) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        Document statusCV = db.findOne(CollectionNameDefs.COLL_STATUS_CV, Filters.eq(DbKeyConfig.ID, request.getStatusCV()));
        if (statusCV == null) {
            response.setFailed("Trạng thái cv không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set(DbKeyConfig.STATUS_CV_ID, request.getStatusCV()),
                Updates.set(DbKeyConfig.STATUS_CV_NAME, statusCV.get(DbKeyConfig.NAME)),
                Updates.set(DbKeyConfig.UPDATE_STATUS_CV_AT, System.currentTimeMillis()),
                Updates.set(DbKeyConfig.UPDATE_STATUS_CV_BY, request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);
        response.setSuccess();

        // insert to rabbitmq
        insertToRabbitMQ("update status profile", request);

        //Insert history to DB
        historyService.createHistory(id, "Cập nhật trạng thái profile", request.getInfo().getFullName());

        return response;
    }

}
