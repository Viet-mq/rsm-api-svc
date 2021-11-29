package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.entities.SourceEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReportRecruitmentEfficiencyServiceImpl extends BaseService implements ReportRecruitmentEfficiencyService{
    @Override
    public GetArrayResponse<ReportByDepartmentEntity> findAll(Long from, Long to) {
        GetArrayStatisticalReponse<ReportByDepartmentEntity> reponse = new GetArrayStatisticalReponse<>();
        List<ReportByDepartmentEntity> rows = new ArrayList<>();

        try {
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
                Set<String> sourceCVNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    sourceCVNames.add(AppUtils.parseString(id.get(DbKeyConfig.SOURCE_CV_NAME)));
                }

                Set<String> recruitmentNames = new HashSet<>();
                for (Document document : lst) {
                    Document id = (Document) document.get(DbKeyConfig._ID);
                    recruitmentNames.add(AppUtils.parseString(id.get(DbKeyConfig.RECRUITMENT_NAME)));
                }

                for (String recruitmentName : recruitmentNames) {
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
                }
            }
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            reponse.setFailed("Hệ thống bận");
            return reponse;
        }
        reponse.setRows(rows);
        reponse.setTotal(rows.size());
        reponse.setSuccess();
        return reponse;
    }

    @Override
    public ExportResponse exportReportRecruitmentEfficiency(Long from, Long to) {
        return null;
    }
}
