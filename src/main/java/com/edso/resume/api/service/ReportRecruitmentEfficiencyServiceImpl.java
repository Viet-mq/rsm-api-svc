package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity;
import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity2;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.ReportRecruitmentEfficiencyExporter;
import com.edso.resume.api.report.ReportRecruitmentEfficiency;
import com.edso.resume.api.report.ReportRecruitmentEfficiency2;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;
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
public class ReportRecruitmentEfficiencyServiceImpl extends BaseService implements ReportRecruitmentEfficiencyService {

    private final ReportRecruitmentEfficiencyExporter exporter;
    @Value("${excel.pathReportRecruitmentEfficiency}")
    private String path;

    protected ReportRecruitmentEfficiencyServiceImpl(MongoDbOnlineSyncActions db, ReportRecruitmentEfficiencyExporter exporter) {
        super(db);
        this.exporter = exporter;
    }

    @Override
    public GetArrayResponse<ReportRecruitmentEfficiencyEntity2> findAll(HeaderInfo info, Long from, Long to) {
        GetArrayResponse<ReportRecruitmentEfficiencyEntity2> response = new GetArrayResponse<>();

        try {
            List<ReportRecruitmentEfficiencyEntity2> rows = new ArrayList<>();

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
            match.append("status_cv_name", new Document().append("$ne", null));
            match.append(DbKeyConfig.ORGANIZATIONS, new Document().append("$in", info.getOrganizations()));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", new Document().append(DbKeyConfig.STATUS_CV_NAME, "$status_cv_name").append(DbKeyConfig.RECRUITMENT_NAME, "$recruitment_name").append(DbKeyConfig.CREATE_RECRUITMENT_BY, "$create_recruitment_by"))
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);

            if (lst != null) {
                Set<String> statusCVName = new HashSet<>();
                Set<Recruitment> recruitmentNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    statusCVName.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
                    Recruitment recruitment = Recruitment.builder()
                            .name(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))
                            .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_RECRUITMENT_BY)))
                            .build();
                    recruitmentNames.add(recruitment);
                }

                CountDownLatch countDownLatch = new CountDownLatch(recruitmentNames.size());
                for (Recruitment recruitment : recruitmentNames) {
                    new Thread(new ReportRecruitmentEfficiency2(statusCVName, lst, recruitment, rows, countDownLatch)).start();
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
    public ExportResponse exportReportRecruitmentEfficiency(HeaderInfo info, Long from, Long to) {
        try {
            List<ReportRecruitmentEfficiencyEntity> rows = new ArrayList<>();

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
            match.append("status_cv_name", new Document().append("$ne", null));
            match.append(DbKeyConfig.ORGANIZATIONS, new Document().append("$in", info.getOrganizations()));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", new Document().append(DbKeyConfig.STATUS_CV_NAME, "$status_cv_name").append(DbKeyConfig.RECRUITMENT_NAME, "$recruitment_name").append(DbKeyConfig.CREATE_RECRUITMENT_BY, "$create_recruitment_by"))
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);

            Set<String> statusCVName = new HashSet<>();
            if (lst != null) {
                Set<Recruitment> recruitmentNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    statusCVName.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
                    Recruitment recruitment = Recruitment.builder()
                            .name(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))
                            .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_RECRUITMENT_BY)))
                            .build();
                    recruitmentNames.add(recruitment);
                }

                CountDownLatch countDownLatch = new CountDownLatch(recruitmentNames.size());
                for (Recruitment recruitment : recruitmentNames) {
                    new Thread(new ReportRecruitmentEfficiency(statusCVName, lst, recruitment, rows, countDownLatch)).start();
                }
                countDownLatch.await();
            }
            return exporter.exportReportRecruitmentEfficiency(rows, statusCVName, path, from, to);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
            return null;
        }
    }
}
