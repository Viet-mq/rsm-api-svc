package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DictionaryNamesEntity;
import com.edso.resume.api.domain.entities.ProfileRabbitMQEntity;
import com.edso.resume.api.domain.entities.RecruitEntity;
import com.edso.resume.api.domain.entities.SourceCVEntity;
import com.edso.resume.api.domain.request.CreateRecruitmentRequest;
import com.edso.resume.api.domain.request.DeleteRecruitmentRequest;
import com.edso.resume.api.domain.request.UpdateRecruitmentRequest;
import com.edso.resume.api.domain.validator.DictionaryValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

@Service
public class RecruitmentServiceImpl extends BaseService implements RecruitmentService, IDictionaryValidator {

    private Queue<DictionaryValidatorResult> queue = new LinkedBlockingQueue<>();

    protected RecruitmentServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<RecruitEntity> findAll(HeaderInfo info, int page, int size) {
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_SOURCE_CV, null, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<RecruitEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                RecruitEntity sourceCV = RecruitEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .title(AppUtils.parseString(doc.get(DbKeyConfig.TITLE)))
                        .levelJobId(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_ID)))
                        .levelJobName(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_NAME)))
                        .address(AppUtils.parseString(doc.get(DbKeyConfig.ADDRESS)))
                        .typeOfJob(AppUtils.parseString(doc.get(DbKeyConfig.TYPE_OF_JOB)))
                        .quantity(AppUtils.parseString(doc.get(DbKeyConfig.QUANTITY)))
                        .detailOfSalary(AppUtils.parseString(doc.get(DbKeyConfig.DETAIL_OF_SALARY)))
                        .jobDescription(AppUtils.parseString(doc.get(DbKeyConfig.JOB_DESCRIPTION)))
                        .requirementOfJob(AppUtils.parseString(doc.get(DbKeyConfig.REQUIREMENT_OF_JOB)))
                        .deadLine(AppUtils.parseLong(doc.get(DbKeyConfig.DEAD_LINE)))
                        .talentPoolId(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_ID)))
                        .talentPoolName(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_NAME)))
                        .interviewer((List<String>) doc.get(DbKeyConfig.INTERVIEWER))
                        .build();
                rows.add(sourceCV);
            }
        }
        GetArrayResponse<RecruitEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(rows.size());
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createRecruitment(CreateRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();

        String idProfile = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        try {

            List<DictionaryValidateProcessor> rs = new ArrayList<>();
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.TALENT_POOL, request.getTalentPool(), db, this));
            rs.add(new DictionaryValidateProcessor(key, ThreadConfig.JOB_LEVEL, request.getLevelJob(), db, this));
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


            // conventions
            Document profile = new Document();
            profile.append(DbKeyConfig.ID, idProfile);
//            profile.append(DbKeyConfig.FULL_NAME, request.getFullName());
//            profile.append(DbKeyConfig.GENDER, request.getGender());
//            profile.append(DbKeyConfig.PHONE_NUMBER, request.getPhoneNumber());
//            profile.append(DbKeyConfig.EMAIL, request.getEmail());
//            profile.append(DbKeyConfig.DATE_OF_BIRTH, request.getDateOfBirth());
//            profile.append(DbKeyConfig.HOMETOWN, request.getHometown());
//            profile.append(DbKeyConfig.SCHOOL_ID, request.getSchool());
//            profile.append(DbKeyConfig.SCHOOL_NAME, dictionaryNames.getSchoolName());
//            profile.append(DbKeyConfig.JOB_ID, request.getJob());
//            profile.append(DbKeyConfig.JOB_NAME, dictionaryNames.getJobName());
//            profile.append(DbKeyConfig.LEVEL_JOB_ID, request.getLevelJob());
//            profile.append(DbKeyConfig.LEVEL_JOB_NAME, dictionaryNames.getLevelJobName());
//            profile.append(DbKeyConfig.SOURCE_CV_ID, request.getSourceCV());
//            profile.append(DbKeyConfig.SOURCE_CV_NAME, dictionaryNames.getSourceCVName());
//            profile.append(DbKeyConfig.HR_REF, request.getHrRef());
//            profile.append(DbKeyConfig.DATE_OF_APPLY, request.getDateOfApply());
//            profile.append(DbKeyConfig.NAME_SEARCH, request.getFullName().toLowerCase());
//            profile.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
//            profile.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
//            profile.append(DbKeyConfig.TALENT_POOL_ID, request.getTalentPool());
//            profile.append(DbKeyConfig.TALENT_POOL_NAME, dictionaryNames.getTalentPoolName());
//            profile.append(DbKeyConfig.DEPARTMENT_ID, request.getDepartment());
//            profile.append(DbKeyConfig.DEPARTMENT_NAME, dictionaryNames.getDepartmentName());
//            profile.append(DbKeyConfig.LEVEL_SCHOOL, request.getLevelSchool());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_PROFILE, profile);

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
    public BaseResponse updateRecruitment(UpdateRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
//                Updates.set(DbKeyConfig.NAME, name),
//                Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
//                Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
//                Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_SCHOOL, cond, updates, true);

        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteRecruitment(DeleteRecruitmentRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq(DbKeyConfig.ID, id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_SCHOOL, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }
        db.delete(CollectionNameDefs.COLL_SCHOOL, cond);
        return new BaseResponse(0, "OK");
    }

    @Override
    public void onValidatorResult(String key, DictionaryValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}
