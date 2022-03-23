package com.edso.resume.api.report;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity2;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ReportRecruitmentActivities2 extends BaseReport implements Runnable {

    private final MongoDbOnlineSyncActions db;
    private final Set<String> statusCVNames;
    private final List<ReportRecruitmentActivitiesEntity2> rows;
    private final AggregateIterable<Document> lst;
    private final Recruitment recruitment;
    private final CountDownLatch countDownLatch;

    public ReportRecruitmentActivities2(MongoDbOnlineSyncActions db, Set<String> statusCVNames, List<ReportRecruitmentActivitiesEntity2> rows, AggregateIterable<Document> lst, Recruitment recruitment, CountDownLatch countDownLatch) {
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
            Map<String, Long> statusEntities = new HashMap<>();
            for (String name : statusCVNames) {
                statusEntities.put(name, 0L);
            }
            for (Map.Entry<String, Long> entry : statusEntities.entrySet()) {
                for (Document doc : lst) {
                    Document id = (Document) doc.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(entry.getKey()) && recruitment.getCreateBy().equals(AppUtils.parseString(id.get(DbKeyConfig.CREATE_BY)))) {
                        entry.setValue(AppUtils.parseLong(doc.get(DbKeyConfig.COUNT)));
                    }
                }
            }
            ReportRecruitmentActivitiesEntity2 reportRecruitmentActivitiesEntity2 = ReportRecruitmentActivitiesEntity2.builder()
                    .fullName(recruitment.getFullNameCreator())
                    .createBy(recruitment.getCreateBy())
                    .recruitmentTotal(db.countAll(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.CREATE_BY, recruitment.getCreateBy())))
                    .noteTotal(db.countAll(CollectionNameDefs.COLL_NOTE_PROFILE, Filters.eq(DbKeyConfig.CREATE_BY, recruitment.getCreateBy())))
                    .status(statusEntities)
                    .build();
            rows.add(reportRecruitmentActivitiesEntity2);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}