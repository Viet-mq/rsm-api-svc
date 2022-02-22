package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.ReportRecruitmentActivitiesExporter;
import com.edso.resume.api.report.ReportRecruitmentActivities;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
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
public class ReportRecruitmentActivitiesServiceImpl extends BaseService implements ReportRecruitmentActivitiesService {

    private final ReportRecruitmentActivitiesExporter exporter;
    @Value("${excel.pathReportRecruitmentActivities}")
    private String path;

    protected ReportRecruitmentActivitiesServiceImpl(MongoDbOnlineSyncActions db, ReportRecruitmentActivitiesExporter exporter) {
        super(db);
        this.exporter = exporter;
    }

    @Override
    public GetArrayResponse<ReportRecruitmentActivitiesEntity> findAll(Long from, Long to) {
        GetArrayResponse<ReportRecruitmentActivitiesEntity> reponse = new GetArrayResponse<>();
        try {
            List<ReportRecruitmentActivitiesEntity> rows = new ArrayList<>();

            List<Bson> c = new ArrayList<>();
            Document query1 = new Document();
            Document match = new Document();

            if (from != null && from > 0) {
                match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$gte", from));
            }

            if (to != null && to > 0) {
                match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$lte", to));
            }

            match.append("create_recruitment_by", new Document().append("$ne", null));
            match.append("status_cv_name", new Document().append("$ne", null));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", new Document().append(DbKeyConfig.STATUS_CV_NAME, "$status_cv_name").append(DbKeyConfig.FULL_NAME_CREATOR, "$full_name_creator").append(DbKeyConfig.CREATE_RECRUITMENT_BY, "$create_recruitment_by"))
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);

            if (lst != null) {
                Set<String> statusCVNames = new HashSet<>();
                Set<Recruitment> recruitmentNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    statusCVNames.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
                    Recruitment recruitment = Recruitment.builder()
                            .fullNameCreator(AppUtils.parseString(id.get(DbKeyConfig.FULL_NAME_CREATOR)))
                            .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_RECRUITMENT_BY)))
                            .build();
                    recruitmentNames.add(recruitment);
                }

                CountDownLatch countDownLatch = new CountDownLatch(recruitmentNames.size());
                for (Recruitment recruitment : recruitmentNames) {
                    new Thread(new ReportRecruitmentActivities(db, statusCVNames, rows, lst, recruitment, countDownLatch)).start();
                }
                countDownLatch.await();
            }
            reponse.setRows(rows);
            reponse.setTotal(rows.size());
            reponse.setSuccess();
            return reponse;
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
            reponse.setFailed("Hệ thống bận");
            return reponse;
        }
    }

    @Override
    public ExportResponse exportReportRecruitmentActivities(Long from, Long to) {
        try {
            List<ReportRecruitmentActivitiesEntity> rows = new ArrayList<>();

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

            match.append("create_recruitment_by", new Document().append("$ne", null));
            match.append("status_cv_name", new Document().append("$ne", null));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", new Document().append(DbKeyConfig.STATUS_CV_NAME, "$status_cv_name").append(DbKeyConfig.FULL_NAME_CREATOR, "$full_name_creator").append(DbKeyConfig.CREATE_RECRUITMENT_BY, "$create_recruitment_by"))
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);
            Set<String> statusCVNames = new HashSet<>();
            if (lst != null) {
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    statusCVNames.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
                }

                Set<Recruitment> recruitmentNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    Recruitment recruitment = Recruitment.builder()
                            .fullNameCreator(AppUtils.parseString(id.get(DbKeyConfig.FULL_NAME_CREATOR)))
                            .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_RECRUITMENT_BY)))
                            .build();
                    recruitmentNames.add(recruitment);
                }

                CountDownLatch countDownLatch = new CountDownLatch(recruitmentNames.size());
                for (Recruitment recruitment : recruitmentNames) {
                    new Thread(new ReportRecruitmentActivities(db, statusCVNames, rows, lst, recruitment, countDownLatch)).start();
                }
                countDownLatch.await();
            }
            return exporter.exportReportRecruitmentActivities(rows, statusCVNames, path, from, to);
        } catch (Throwable ex) {
            logger.error("Ex :", ex);
            return null;
        }
    }
}