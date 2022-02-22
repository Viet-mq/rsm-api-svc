package com.edso.resume.api.report;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.entities.SourceEntity;
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

public class ReportByDepartment implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Set<String> sourceCVNames;
    private final List<ReportByDepartmentEntity> rows;
    private final AggregateIterable<Document> lst;
    private final String recruitmentName;
    private final CountDownLatch countDownLatch;

    public ReportByDepartment(Set<String> sourceCVNames, List<ReportByDepartmentEntity> rows, AggregateIterable<Document> lst, String recruitmentName, CountDownLatch countDownLatch) {
        this.sourceCVNames = sourceCVNames;
        this.rows = rows;
        this.lst = lst;
        this.recruitmentName = recruitmentName;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            List<SourceEntity> sourceEntities = new ArrayList<>();
            for (String name : sourceCVNames) {
                SourceEntity sourceEntity = SourceEntity.builder()
                        .sourceCVName(name)
                        .count(0L)
                        .build();
                sourceEntities.add(sourceEntity);
            }
            for (SourceEntity sourceEntity : sourceEntities) {
                for (Document doc : lst) {
                    Document id = (Document) doc.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.SOURCE_CV_NAME)).equals(sourceEntity.getSourceCVName()) && recruitmentName.equals(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))) {
                        sourceEntity.setCount(AppUtils.parseLong(doc.get(DbKeyConfig.COUNT)));
                    }
                }
            }
            ReportByDepartmentEntity reportByDepartmentEntity = ReportByDepartmentEntity.builder()
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
