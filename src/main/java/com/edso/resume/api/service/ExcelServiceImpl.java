package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.entities.SkillEntity;
import com.edso.resume.api.exporter.ProfilesExporter;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ExcelServiceImpl extends BaseService implements ExcelService {

    private final ProfilesExporter profilesExporter;

    @Value("${excel.path}")
    private String path;

    protected ExcelServiceImpl(MongoDbOnlineSyncActions db, ProfilesExporter profilesExporter) {
        super(db);
        this.profilesExporter = profilesExporter;
    }


    @Override
    public String exportExcel(HeaderInfo info, String fullName, String talentPool, String job, String levelJob, String department, String recruitment, String calendar, String statusCV) {
        try {
            List<Bson> c = new ArrayList<>();
            if (!Strings.isNullOrEmpty(fullName)) {
                c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(fullName))));
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
            FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, sort, 0, 0);
            List<ProfileEntity> rows = new ArrayList<>();
            if (lst != null) {
                for (Document doc : lst) {

                    List<SkillEntity> list = new ArrayList<>();
                    List<Document> documentList = (List<Document>) doc.get(DbKeyConfig.SKILL);
                    if (documentList != null && !documentList.isEmpty()) {
                        for (Document document : documentList) {
                            SkillEntity skillEntity = SkillEntity.builder()
                                    .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                                    .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                                    .build();
                            list.add(skillEntity);
                        }
                    }

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
                            .recruitmentId(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_ID)))
                            .recruitmentName(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_NAME)))
                            .mailRef(AppUtils.parseString(doc.get(DbKeyConfig.MAIL_REF)))
                            .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                            .skill(list)
                            .avatarColor(AppUtils.parseString(doc.get(DbKeyConfig.AVATAR_COLOR)))
                            .isNew(AppUtils.parseString(doc.get(DbKeyConfig.IS_NEW)))
                            .build();
                    rows.add(profile);
                }
            }
            return ProfilesExporter.writeExcel(rows, path);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            return null;
        }
    }
}
