package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.Recruitment;
import com.edso.resume.api.domain.entities.ReportRecruitmentEfficiencyEntity;
import com.edso.resume.api.domain.entities.StatusEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.ReportRecruitmentEfficiencyExporter;
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
public class ReportRecruitmentEfficiencyServiceImpl extends BaseService implements ReportRecruitmentEfficiencyService {

    private final ReportRecruitmentEfficiencyExporter exporter;
    @Value("${excel.pathReportRecruitmentEfficiency}")
    private String path;

    protected ReportRecruitmentEfficiencyServiceImpl(MongoDbOnlineSyncActions db, ReportRecruitmentEfficiencyExporter exporter) {
        super(db);
        this.exporter = exporter;
    }

    @Override
    public GetArrayResponse<ReportRecruitmentEfficiencyEntity> findAll(Long from, Long to) {
        GetArrayResponse<ReportRecruitmentEfficiencyEntity> reponse = new GetArrayResponse<>();
        List<ReportRecruitmentEfficiencyEntity> rows = new ArrayList<>();

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
            for (Document document : lst) {
                Document id = (Document) document.get(DbKeyConfig._ID);
                statusCVName.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
            }

            Set<Recruitment> recruitmentNames = new HashSet<>();
            for (Document document : lst) {
                Document id = (Document) document.get(DbKeyConfig._ID);
                Recruitment recruitment = Recruitment.builder()
                        .name(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))
                        .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_RECRUITMENT_BY)))
                        .build();
                recruitmentNames.add(recruitment);
            }

            for (Recruitment recruitment : recruitmentNames) {
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
            }
        }
        reponse.setRows(rows);
        reponse.setTotal(rows.size());
        reponse.setSuccess();
        return reponse;
    }

    @Override
    public ExportResponse exportReportRecruitmentEfficiency(Long from, Long to) {
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
            for (Document document : lst) {
                Document id = (Document) document.get(DbKeyConfig._ID);
                statusCVName.add(AppUtils.parseString(id.get(DbKeyConfig.STATUS_CV_NAME)));
            }

            Set<Recruitment> recruitmentNames = new HashSet<>();
            for (Document document : lst) {
                Document id = (Document) document.get(DbKeyConfig._ID);
                Recruitment recruitment = Recruitment.builder()
                        .name(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)))
                        .createBy(AppUtils.parseString(id.get(DbKeyConfig.CREATE_RECRUITMENT_BY)))
                        .build();
                recruitmentNames.add(recruitment);
            }

            for (Recruitment recruitment : recruitmentNames) {
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
            }
        }
        return exporter.exportReportRecruitmentEfficiency(rows, statusCVName, path, from, to);
    }
}
