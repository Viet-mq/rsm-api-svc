package com.edso.resume.api.report;

import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity2;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ReportRecruitmentEfficiency2 extends BaseReport implements Runnable {

    private final Set<String> statusCVName;
    private final AggregateIterable<Document> lst;
    private final Recruitment recruitment;
    private final List<ReportRecruitmentEfficiencyEntity2> rows;
    private final CountDownLatch countDownLatch;

    public ReportRecruitmentEfficiency2(Set<String> statusCVName, AggregateIterable<Document> lst, Recruitment recruitment, List<ReportRecruitmentEfficiencyEntity2> rows, CountDownLatch countDownLatch) {
        this.statusCVName = statusCVName;
        this.lst = lst;
        this.recruitment = recruitment;
        this.rows = rows;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Map<String, Long> statusEntities = new HashMap<>();
            for (String name : statusCVName) {
                statusEntities.put(name, 0L);
            }
            for (Map.Entry<String, Long> entry : statusEntities.entrySet()) {
                for (Document doc : lst) {
                    Document id = (Document) doc.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(entry.getKey()) && recruitment.getName().equals(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))) {
                        entry.setValue(AppUtils.parseLong(doc.get(DbKeyConfig.COUNT)));
                    }
                }
            }
            ReportRecruitmentEfficiencyEntity2 reportRecruitmentEfficiencyEntity2 = ReportRecruitmentEfficiencyEntity2.builder()
                    .recruitmentName(recruitment.getName())
                    .createBy(recruitment.getCreateBy())
                    .status(statusEntities)
                    .build();
            rows.add(reportRecruitmentEfficiencyEntity2);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}

