package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ReportRecruitmentResultEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.api.exporter.ReportRecruitmentResultExporter;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.response.GetArrayResponse;
import com.mongodb.client.model.Filters;
import org.bson.Document;
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
    public GetArrayResponse<ReportRecruitmentResultEntity> findAll(Long from, Long to) {
        GetArrayResponse<ReportRecruitmentResultEntity> reponse = new GetArrayResponse<>();
        List<ReportRecruitmentResultEntity> rows = new ArrayList<>();

        List<Document> lst = db.findAll(CollectionNameDefs.COLL_RECRUITMENT, null, null, 0, 0);

        if (lst != null) {
            for (Document document : lst) {
                int needToRecruit = AppUtils.parseInt(document.get(DbKeyConfig.QUANTITY));
                int recruited = (int) db.countAll(CollectionNameDefs.COLL_PROFILE, Filters.and(Filters.eq(DbKeyConfig.STATUS_CV_ID, idPass), Filters.eq(DbKeyConfig.RECRUITMENT_ID, document.get(DbKeyConfig.ID))));
                double persent = (double) recruited / (double) needToRecruit * 100;
                ReportRecruitmentResultEntity reportRecruitmentResultEntity = ReportRecruitmentResultEntity.builder()
                        .recruitmentName(AppUtils.parseString(document.get(DbKeyConfig.TITLE)))
                        .needToRecruit(needToRecruit)
                        .recruited(recruited)
                        .percent(String.format("%.2f", persent) + "%")
                        .build();
                rows.add(reportRecruitmentResultEntity);
            }
        }
        reponse.setRows(rows);
        reponse.setTotal(rows.size());
        reponse.setSuccess();
        return reponse;
    }

    @Override
    public ExportResponse exportReportRecruitmentResult(Long from, Long to) {
        List<ReportRecruitmentResultEntity> rows = new ArrayList<>();

        List<Document> lst = db.findAll(CollectionNameDefs.COLL_RECRUITMENT, null, null, 0, 0);

        if (lst != null) {
            for (Document document : lst) {
                int needToRecruit = AppUtils.parseInt(document.get(DbKeyConfig.QUANTITY));
                int recruited = (int) db.countAll(CollectionNameDefs.COLL_PROFILE, Filters.and(Filters.eq(DbKeyConfig.STATUS_CV_ID, idPass), Filters.eq(DbKeyConfig.RECRUITMENT_ID, document.get(DbKeyConfig.ID))));
                double persent = (double) recruited / (double) needToRecruit * 100;
                ReportRecruitmentResultEntity reportRecruitmentResultEntity = ReportRecruitmentResultEntity.builder()
                        .recruitmentName(AppUtils.parseString(document.get(DbKeyConfig.TITLE)))
                        .needToRecruit(needToRecruit)
                        .recruited(recruited)
                        .percent(String.format("%.2f", persent) + "%")
                        .build();
                rows.add(reportRecruitmentResultEntity);
            }
        }
        return exporter.exportReportRecruitmentActivities(rows, path, from, to);
    }
}
