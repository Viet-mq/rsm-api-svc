package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.StatisticalEntity;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticalServiceImpl extends BaseService implements StatisticalService {

    protected StatisticalServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayStatisticalReponse<StatisticalEntity> findAll(HeaderInfo info, Long from, Long to, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (from != null && from > 0) {
            c.add(Filters.gte(DbKeyConfig.RECRUITMENT_TIME, from));
        }
        if (from != null && from > 0) {
            c.add(Filters.lte(DbKeyConfig.RECRUITMENT_TIME, to));
        }
        List<Bson> cond = new ArrayList<>();
        Document query = new Document();
        query.append("$group", new Document()
                .append("_id", new Document().append("source_cv_name", "$source_cv_name").append("recruitment_name", "$recruitment_name"))
                .append("count", new Document()
                        .append("$sum", 1.0)
                )
        );
        cond.add(query);
        AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, cond);
        System.out.println(lst);
//        Bson cond = buildCondition(c);
//        PagingInfo pagingInfo = PagingInfo.parse(page, size);
//        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
//        List<StatisticalEntity> rows = new ArrayList<>();
//        if (lst != null) {
//            for (Document doc : lst) {
//                String title = AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_NAME));
//                if (!title.isEmpty()) {
//                    boolean checkTitle = false;
//                    if (!rows.isEmpty()) {
//                        for (StatisticalEntity statisticalEntity : rows) {
//                            if (statisticalEntity.getTitle().equals(title)) {
//                                checkTitle = true;
//                            }
//                        }
//                    }
//                    if (!checkTitle) {
//                        FindIterable<Document> list = db.findAll2(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.RECRUITMENT_NAME, title), null, pagingInfo.getStart(), pagingInfo.getLimit());
//                        List<SourceEntity> sources = new ArrayList<>();
//                        for (Document document : list) {
//                            String sourceName = AppUtils.parseString(document.get(DbKeyConfig.SOURCE_CV_NAME));
//                            boolean checkSource = false;
//                            if (!sources.isEmpty()) {
//                                for (SourceEntity sourceEntity : sources) {
//                                    if (sourceEntity.getName().equals(sourceName)) {
//                                        checkSource = true;
//                                    }
//                                }
//                            }
//                            if (!checkSource) {
//                                SourceEntity sourceEntity = new SourceEntity();
//                                sourceEntity.setName();
//                                sourceEntity.setCount(db.countAll(CollectionNameDefs.COLL_PROFILE,Filters.and(Filters.eq())));
//                            }
//                            StatisticalEntity statusCV = StatisticalEntity.builder().title(title).sources(source).build();
//                            rows.add(statusCV);
//                        }
//                    }
//                }
//            }
//        }
//        GetArrayStatisticalReponse<StatisticalEntity> resp = new GetArrayStatisticalReponse<>();
//        resp.setSuccess();
//        resp.setTotal(db.countAll(CollectionNameDefs.COLL_PROFILE, cond));
//        resp.setRows(rows);
        return null;
    }
}
