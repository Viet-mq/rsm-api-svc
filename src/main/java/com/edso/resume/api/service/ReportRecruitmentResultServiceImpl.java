package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ReportRecruitmentResultEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.ReportRecruitmentResultExporter;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportRecruitmentResultServiceImpl extends BaseService implements ReportRecruitmentResultService {

    private final ReportRecruitmentResultExporter exporter;
    @Value("${id.pass}")
    private String idPass;
    @Value("${excel.pathReportRecruitmentResult}")
    private String path;

    protected ReportRecruitmentResultServiceImpl(MongoDbOnlineSyncActions db, ReportRecruitmentResultExporter exporter) {
        super(db);
        this.exporter = exporter;
    }


    @Override
    public GetArrayResponse<ReportRecruitmentResultEntity> findAll(HeaderInfo info, Long from, Long to) {
        GetArrayResponse<ReportRecruitmentResultEntity> response = new GetArrayResponse<>();
        List<ReportRecruitmentResultEntity> rows = new ArrayList<>();
        List<Bson> c = new ArrayList<>();
        if (from != null) {
            c.add(Filters.gte(DbKeyConfig.CREATE_AT, from));
        }
        if (to != null) {
            c.add(Filters.lte(DbKeyConfig.CREATE_AT, to));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        List<Document> lst = db.findAll(CollectionNameDefs.COLL_RECRUITMENT, cond, null, 0, 0);

        if (lst != null) {
            for (Document document : lst) {
                int needToRecruit = AppUtils.parseInt(document.get(DbKeyConfig.QUANTITY));
                int recruited = (int) db.countAll(CollectionNameDefs.COLL_PROFILE, Filters.and(Filters.eq(DbKeyConfig.STATUS_CV_ID, idPass), Filters.eq(DbKeyConfig.RECRUITMENT_ID, document.get(DbKeyConfig.ID))));
                double percent = (double) recruited / (double) needToRecruit * 100;
                ReportRecruitmentResultEntity reportRecruitmentResultEntity = ReportRecruitmentResultEntity.builder()
                        .recruitmentName(AppUtils.parseString(document.get(DbKeyConfig.TITLE)))
                        .needToRecruit(needToRecruit)
                        .recruited(recruited)
                        .percent(String.format("%.2f", percent) + "%")
                        .build();
                rows.add(reportRecruitmentResultEntity);
            }
        }
        response.setRows(rows);
        response.setTotal(rows.size());
        response.setSuccess();
        return response;
    }

    @Override
    public ExportResponse exportReportRecruitmentResult(HeaderInfo info, Long from, Long to) {
        List<ReportRecruitmentResultEntity> rows = new ArrayList<>();
        List<Bson> c = new ArrayList<>();
        if (from != null) {
            c.add(Filters.gte(DbKeyConfig.CREATE_AT, from));
        } else {
            from = 0L;
        }
        if (to != null) {
            c.add(Filters.lte(DbKeyConfig.CREATE_AT, to));
        } else {
            to = System.currentTimeMillis();
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        List<Document> lst = db.findAll(CollectionNameDefs.COLL_RECRUITMENT, cond, null, 0, 0);

        if (lst != null) {
            for (Document document : lst) {
                int needToRecruit = AppUtils.parseInt(document.get(DbKeyConfig.QUANTITY));
                int recruited = (int) db.countAll(CollectionNameDefs.COLL_PROFILE, Filters.and(Filters.eq(DbKeyConfig.STATUS_CV_ID, idPass), Filters.eq(DbKeyConfig.RECRUITMENT_ID, document.get(DbKeyConfig.ID))));
                double percent = (double) recruited / (double) needToRecruit * 100;
                ReportRecruitmentResultEntity reportRecruitmentResultEntity = ReportRecruitmentResultEntity.builder()
                        .recruitmentName(AppUtils.parseString(document.get(DbKeyConfig.TITLE)))
                        .needToRecruit(needToRecruit)
                        .recruited(recruited)
                        .percent(String.format("%.2f", percent) + "%")
                        .build();
                rows.add(reportRecruitmentResultEntity);
            }
        }
        return exporter.exportReportRecruitmentActivities(rows, path, from, to);
    }
}
