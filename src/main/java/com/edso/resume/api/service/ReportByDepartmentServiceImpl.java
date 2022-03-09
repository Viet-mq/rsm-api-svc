package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.entities.ReportByDepartmentEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.PositionResumeExporter;
import com.edso.resume.api.report.ReportByDepartment;
import com.edso.resume.api.report.ReportByDepartment2;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Service
public class ReportByDepartmentServiceImpl extends BaseService implements ReportByDepartmentService {

    private final PositionResumeExporter exporter;
    @Value("${excel.pathReportByDepartment}")
    private String path;

    protected ReportByDepartmentServiceImpl(MongoDbOnlineSyncActions db, PositionResumeExporter exporter) {
        super(db);
        this.exporter = exporter;
    }

    @Override
    public GetArrayStatisticalReponse<ReportByDepartmentEntity2> findAll(Long from, Long to) {
        GetArrayStatisticalReponse<ReportByDepartmentEntity2> response = new GetArrayStatisticalReponse<>();
        try {
            List<ReportByDepartmentEntity2> rows = new ArrayList<>();

            List<Bson> c = new ArrayList<>();
            Document query1 = new Document();
            Document match = new Document();

            if (from != null && from > 0) {
                match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$gte", from));
            }

            if (to != null && to > 0) {
                match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$lte", to));
            }

            match.append("recruitment_name", new Document().append("$ne", null));
            match.append("source_cv_name", new Document().append("$ne", null));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", new Document().append(DbKeyConfig.SOURCE_CV_NAME, "$source_cv_name").append(DbKeyConfig.RECRUITMENT_NAME, "$recruitment_name"))
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);

            if (lst != null) {
                Set<String> recruitmentNames = new HashSet<>();
                Set<String> sourceCVNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    sourceCVNames.add(AppUtils.parseString(id.get(DbKeyConfig.SOURCE_CV_NAME)));
                    recruitmentNames.add(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)));
                }

                CountDownLatch countDownLatch = new CountDownLatch(recruitmentNames.size());
                for (String recruitmentName : recruitmentNames) {
                    new Thread(new ReportByDepartment2(sourceCVNames, rows, lst, recruitmentName, countDownLatch)).start();
                }
                countDownLatch.await();
            }
            response.setRows(rows);
            response.setTotal(rows.size());
            response.setSuccess();
            return response;
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
            response.setFailed("Hệ thống bận");
            return response;
        }
    }

    @Override
    public ExportResponse exportReportByDepartment(Long from, Long to) {
        try {
            List<ReportByDepartmentEntity> rows = new ArrayList<>();
            List<Bson> c = new ArrayList<>();
            Document query1 = new Document();
            Document match = new Document();

            if (from != null && from > 0) {
                match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$gte", from));
            } else {
                from = 0L;
            }

            if (to != null && to > 0) {
                match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$lte", to));
            } else {
                to = System.currentTimeMillis();
            }

            match.append("recruitment_name", new Document().append("$ne", null));
            match.append("source_cv_name", new Document().append("$ne", null));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", new Document().append(DbKeyConfig.SOURCE_CV_NAME, "$source_cv_name").append(DbKeyConfig.RECRUITMENT_NAME, "$recruitment_name"))
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);
            Set<String> sourceCVNames = new HashSet<>();
            if (lst != null) {
                Set<String> recruitmentNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    sourceCVNames.add(AppUtils.parseString(id.get(DbKeyConfig.SOURCE_CV_NAME)));
                    recruitmentNames.add(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)));
                }

                CountDownLatch countDownLatch = new CountDownLatch(recruitmentNames.size());
                for (String recruitmentName : recruitmentNames) {
                    new Thread(new ReportByDepartment(sourceCVNames, rows, lst, recruitmentName, countDownLatch)).start();
                }
                countDownLatch.await();
            }
            return exporter.exportReportByDepartment(rows, sourceCVNames, path, from, to);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
            return null;
        }
    }
}
