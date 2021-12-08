package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Reason;
import com.edso.resume.api.domain.entities.ReportRejectProfileEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.ReportRejectProfileExporter;
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

@Service
public class ReportRejectProfileServiceImpl extends BaseService implements ReportRejectProfileService {

    private final ReportRejectProfileExporter exporter;
    @Value("${excel.pathReportRejectProfile}")
    private String path;

    protected ReportRejectProfileServiceImpl(MongoDbOnlineSyncActions db, ReportRejectProfileExporter exporter) {
        super(db);
        this.exporter = exporter;
    }

    @Override
    public GetArrayResponse<ReportRejectProfileEntity> findAll(Long from, Long to) {
        GetArrayResponse<ReportRejectProfileEntity> reponse = new GetArrayResponse<>();
        List<ReportRejectProfileEntity> rows = new ArrayList<>();

        List<Bson> c = new ArrayList<>();
        Document query1 = new Document();
        Document match = new Document();

        if (from != null && from > 0) {
            match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$gte", from));
        }

        if (to != null && to > 0) {
            match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$lte", to));
        }
        match.append("status_cv_name", new Document().append("$ne", null));
        query1.append("$match", match);

        Document query2 = new Document();
        query2.append("$group", new Document()
                .append("_id", new Document().append(DbKeyConfig.STATUS_CV_NAME, "$status_cv_name").append(DbKeyConfig.REASON, "$reason"))
                .append("count", new Document().append("$sum", 1)
                )
        );

        c.add(query1);
        c.add(query2);

        AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_REASON_REJECT_PROFILE, c);

        if (lst != null) {
            Set<String> statusCVName = new HashSet<>();
            for (Document document : lst) {
                Document id = (Document) document.get(DbKeyConfig._ID);
                statusCVName.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
            }

            for (String status : statusCVName) {
                long total = 0;
                List<Reason> reasons = new ArrayList<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(status)) {
                        total += AppUtils.parseLong(document.get(DbKeyConfig.COUNT));
                    }
                }

                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(status)) {
                        long count = AppUtils.parseLong(document.get(DbKeyConfig.COUNT));
                        Reason reason = Reason.builder()
                                .reason(AppUtils.parseString(id.get(DbKeyConfig.REASON)))
                                .count(count)
                                .percent(String.format("%.2f", (double) count / (double) total * 100) + "%")
                                .build();
                        reasons.add(reason);
                    }
                }

                ReportRejectProfileEntity reportRecruitmentEfficiencyEntity = ReportRejectProfileEntity.builder()
                        .sheet(status)
                        .reasons(reasons)
                        .total(total)
                        .build();
                rows.add(reportRecruitmentEfficiencyEntity);
            }
        }
        reponse.setRows(rows);
        reponse.setTotal(rows.size());
        reponse.setSuccess();
        return reponse;
    }

    @Override
    public ExportResponse exportReportRejectProfile(Long from, Long to) {
        List<ReportRejectProfileEntity> rows = new ArrayList<>();

        List<Bson> c = new ArrayList<>();
        Document query1 = new Document();
        Document match = new Document();

        if (from != null && from > 0) {
            match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$gte", from));
        }

        if (to != null && to > 0) {
            match.append(DbKeyConfig.RECRUITMENT_TIME, new Document().append("$lte", to));
        }
        match.append("status_cv_name", new Document().append("$ne", null));
        query1.append("$match", match);

        Document query2 = new Document();
        query2.append("$group", new Document()
                .append("_id", new Document().append(DbKeyConfig.STATUS_CV_NAME, "$status_cv_name").append(DbKeyConfig.REASON, "$reason"))
                .append("count", new Document().append("$sum", 1)
                )
        );

        c.add(query1);
        c.add(query2);

        AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_REASON_REJECT_PROFILE, c);

        if (lst != null) {
            Set<String> statusCVName = new HashSet<>();
            for (Document document : lst) {
                Document id = (Document) document.get(DbKeyConfig._ID);
                statusCVName.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
            }

            for (String status : statusCVName) {
                long total = 0;
                List<Reason> reasons = new ArrayList<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(status)) {
                        total += AppUtils.parseLong(document.get(DbKeyConfig.COUNT));
                    }
                }

                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    if (AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)).equals(status)) {
                        long count = AppUtils.parseLong(document.get(DbKeyConfig.COUNT));
                        Reason reason = Reason.builder()
                                .reason(AppUtils.parseString(id.get(DbKeyConfig.REASON)))
                                .count(count)
                                .percent(String.format("%.2f", (double) count / (double) total * 100) + "%")
                                .build();
                        reasons.add(reason);
                    }
                }

                ReportRejectProfileEntity reportRecruitmentEfficiencyEntity = ReportRejectProfileEntity.builder()
                        .sheet(status)
                        .reasons(reasons)
                        .total(total)
                        .build();
                rows.add(reportRecruitmentEfficiencyEntity);
            }
        }
        return exporter.exportReportRejectProfile(rows, path, from, to);
    }
}
