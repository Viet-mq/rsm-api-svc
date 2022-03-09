package com.edso.resume.api.report;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity2;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ReportByDepartment2 extends BaseReport implements Runnable {

    private final Set<String> sourceCVNames;
    private final List<ReportByDepartmentEntity2> rows;
    private final AggregateIterable<Document> lst;
    private final String recruitmentName;
    private final CountDownLatch countDownLatch;

    public ReportByDepartment2(Set<String> sourceCVNames, List<ReportByDepartmentEntity2> rows, AggregateIterable<Document> lst, String recruitmentName, CountDownLatch countDownLatch) {
        this.sourceCVNames = sourceCVNames;
        this.rows = rows;
        this.lst = lst;
        this.recruitmentName = recruitmentName;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Map<String, Long> sourceEntities = new HashMap<>();
            for (String name : sourceCVNames) {
                sourceEntities.put(name, 0L);
            }
            for (Map.Entry<String, Long> entry : sourceEntities.entrySet()) {
                for (Document doc : lst) {
                    Document id = (Document) doc.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.SOURCE_CV_NAME)).equals(entry.getKey()) && recruitmentName.equals(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))) {
                        entry.setValue(AppUtils.parseLong(doc.get(DbKeyConfig.COUNT)));
                    }
                }
            }
            ReportByDepartmentEntity2 reportByDepartmentEntity = ReportByDepartmentEntity2.builder()
                    .recruitmentName(recruitmentName)
                    .sources(sourceEntities)
                    .build();
            rows.add(reportByDepartmentEntity);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}