package com.edso.resume.api.report;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity;
import com.edso.resume.api.domain.entities.StatusEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ReportRecruitmentActivities implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MongoDbOnlineSyncActions db;
    private final Set<String> statusCVNames;
    private final List<ReportRecruitmentActivitiesEntity> rows;
    private final AggregateIterable<Document> lst;
    private final Recruitment recruitment;
    private final CountDownLatch countDownLatch;

    public ReportRecruitmentActivities(MongoDbOnlineSyncActions db, Set<String> statusCVNames, List<ReportRecruitmentActivitiesEntity> rows, AggregateIterable<Document> lst, Recruitment recruitment, CountDownLatch countDownLatch) {
        this.db = db;
        this.statusCVNames = statusCVNames;
        this.rows = rows;
        this.lst = lst;
        this.recruitment = recruitment;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            List<StatusEntity> statusEntities = new ArrayList<>();
            for (String name : statusCVNames) {
                StatusEntity statusEntity = StatusEntity.builder()
                        .statusCVName(name)
                        .count(0L)
                        .build();
                statusEntities.add(statusEntity);
            }
            for (StatusEntity statusEntity : statusEntities) {
                for (Document doc : lst) {
                    Document id = (Document) doc.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(statusEntity.getStatusCVName()) && recruitment.getFullNameCreator().equals(AppUtils.parseString(id.get(DbKeyConfig.FULL_NAME_CREATOR)))) {
                        statusEntity.setCount(AppUtils.parseLong(doc.get(DbKeyConfig.COUNT)));
                    }
                }
            }
            ReportRecruitmentActivitiesEntity reportRecruitmentActivitiesEntity = ReportRecruitmentActivitiesEntity.builder()
                    .fullName(recruitment.getFullNameCreator())
                    .createBy(recruitment.getCreateBy())
                    .recruitmentTotal(db.countAll(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.CREATE_BY, recruitment.getCreateBy())))
                    .noteTotal(db.countAll(CollectionNameDefs.COLL_NOTE_PROFILE, Filters.eq(DbKeyConfig.CREATE_BY, recruitment.getCreateBy())))
                    .status(statusEntities)
                    .build();
            rows.add(reportRecruitmentActivitiesEntity);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}
