package com.edso.resume.api.report;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ReportRecruitmentActivities3 extends BaseReport implements Runnable {
    private final Set<String> statusCVNames;
    private final Set<Recruitment> recruitmentNames;
    private final Document document;
    private final MongoDbOnlineSyncActions db;
    private final CountDownLatch countDownLatch;

    public ReportRecruitmentActivities3(Set<String> statusCVNames, Set<Recruitment> recruitmentNames, Document document, MongoDbOnlineSyncActions db, CountDownLatch countDownLatch) {
        this.statusCVNames = statusCVNames;
        this.recruitmentNames = recruitmentNames;
        this.document = document;
        this.db = db;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Document id = (Document) document.get(DbKeyConfig._ID);
            statusCVNames.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
            Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, id.get(DbKeyConfig.CREATE_BY)));
            Recruitment recruitment = Recruitment.builder()
                    .fullNameCreator(AppUtils.parseString(AppUtils.parseString(user.get(DbKeyConfig.FULL_NAME))))
                    .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_BY)))
                    .build();
            recruitmentNames.add(recruitment);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}
