package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.SkillEntity;
import com.edso.resume.api.domain.request.CreateSkillRequest;
import com.edso.resume.api.domain.request.DeleteSkillRequest;
import com.edso.resume.api.domain.request.UpdateSkillRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ErrorCodeDefs;
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
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SkillServiceImpl extends BaseService implements SkillService {


    protected SkillServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<SkillEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
//        c.add(Filters.eq(DbKeyConfig.STATUS, NameConfig.DANG_SU_DUNG));
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_SKILL, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<SkillEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                SkillEntity skill = SkillEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
//                        .jobs((List<CategoryEntity>) doc.get(DbKeyConfig.JOBS))
//                        .status(AppUtils.parseString(doc.get(DbKeyConfig.STATUS)))
                        .build();
                rows.add(skill);
            }
        }
        GetArrayResponse<SkillEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_SKILL, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createSkill(CreateSkillRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName();
            Bson c = Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            long count = db.countAll(CollectionNameDefs.COLL_SKILL, c);

            if (count > 0) {
                response.setResult(ErrorCodeDefs.NAME, "Tên này đã tồn tại");
                return response;
            }

//            List<Document> jobs = null;
//            if (request.getJobs() != null && !request.getJobs().isEmpty()) {
//                for (String jobId : request.getJobs()) {
//                    Bson cond = Filters.eq(DbKeyConfig.ID, jobId);
//                    Document doc = db.findOne(CollectionNameDefs.COLL_JOB, cond);
//                    if (doc == null) {
//                        response.setResult(ErrorCodeDefs.JOB, "Không tồn tại vị trí công việc này");
//                        return response;
//                    }
//                    jobs = new ArrayList<>();
//                    Document job = new Document();
//                    job.append(DbKeyConfig.ID, doc.get(DbKeyConfig.ID));
//                    job.append(DbKeyConfig.NAME, doc.get(DbKeyConfig.NAME));
//                    jobs.add(job);
//                }
//            }

            Document skill = new Document();
            skill.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            skill.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
//            skill.append(DbKeyConfig.JOBS, jobs);
//            skill.append(DbKeyConfig.STATUS, NameConfig.DANG_SU_DUNG);
            skill.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            skill.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            skill.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            skill.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            skill.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            skill.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_SKILL, skill);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }


    @Override
    public BaseResponse updateSkill(UpdateSkillRequest request) {

        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_SKILL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_SKILL, Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

//            List<Document> jobs = null;
//            if (request.getJobs() != null && !request.getJobs().isEmpty()) {
//                for (String jobId : request.getJobs()) {
//                    Bson con = Filters.eq(DbKeyConfig.ID, jobId);
//                    Document doc = db.findOne(CollectionNameDefs.COLL_JOB, con);
//                    if (doc == null) {
//                        response.setResult(ErrorCodeDefs.JOB, "Không tồn tại vị trí công việc này");
//                        return response;
//                    }
//                    jobs = new ArrayList<>();
//                    Document job = new Document();
//                    job.append(DbKeyConfig.ID, doc.get(DbKeyConfig.ID));
//                    job.append(DbKeyConfig.NAME, doc.get(DbKeyConfig.NAME));
//                    jobs.add(job);
//                }
//            }

            Bson idSkill = Filters.eq(DbKeyConfig.SKILL_ID, request.getId());
            Bson updateProfile = Updates.combine(
                    Updates.set(DbKeyConfig.SKILL_NAME, AppUtils.mergeWhitespace(name))
            );
            db.update(CollectionNameDefs.COLL_PROFILE, idSkill, updateProfile);

            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
//                    Updates.set(DbKeyConfig.JOBS, jobs),
//                    Updates.set(DbKeyConfig.STATUS, request.getStatus()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_SKILL, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteSkill(DeleteSkillRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document skill = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.SKILL_ID, request.getId()));
            if (skill == null) {
                String id = request.getId();
                Bson cond = Filters.eq(DbKeyConfig.ID, id);
                Document idDocument = db.findOne(CollectionNameDefs.COLL_SKILL, cond);

                if (idDocument == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
                db.delete(CollectionNameDefs.COLL_SKILL, cond);
                response.setSuccess();
                return response;
            } else {
                response.setFailed("Không thể xóa kỹ năng công việc này!");
                return response;
            }
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
