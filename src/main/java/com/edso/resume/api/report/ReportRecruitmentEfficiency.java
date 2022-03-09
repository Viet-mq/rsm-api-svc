package com.edso.resume.api.report;

import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity;
import com.edso.resume.api.domain.entities.StatusEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ReportRecruitmentEfficiency extends BaseReport implements Runnable {

    private final Set<String> statusCVName;
    private final AggregateIterable<Document> lst;
    private final Recruitment recruitment;
    private final List<ReportRecruitmentEfficiencyEntity> rows;
    private final CountDownLatch countDownLatch;

    public ReportRecruitmentEfficiency(Set<String> statusCVName, AggregateIterable<Document> lst, Recruitment recruitment, List<ReportRecruitmentEfficiencyEntity> rows, CountDownLatch countDownLatch) {
        this.statusCVName = statusCVName;
        this.lst = lst;
        this.recruitment = recruitment;
        this.rows = rows;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            List<StatusEntity> statusEntities = new ArrayList<>();
            for (String name : statusCVName) {
                StatusEntity statusEntity = StatusEntity.builder()
                        .statusCVName(name)
                        .count(0L)
                        .build();
                statusEntities.add(statusEntity);
            }
            for (StatusEntity statusEntity : statusEntities) {
                for (Document doc : lst) {
                    Document id = (Document) doc.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(statusEntity.getStatusCVName()) && recruitment.getName().equals(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))) {
                        statusEntity.setCount(AppUtils.parseLong(doc.get(DbKeyConfig.COUNT)));
                    }
                }
            }
            ReportRecruitmentEfficiencyEntity reportRecruitmentEfficiencyEntity = ReportRecruitmentEfficiencyEntity.builder()
                    .recruitmentName(recruitment.getName())
                    .createBy(recruitment.getCreateBy())
                    .status(statusEntities)
                    .build();
            rows.add(reportRecruitmentEfficiencyEntity);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}
