package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.StatisticalEntity;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.GetArrayStatisticalReponse;
import com.mongodb.client.FindIterable;
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
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<StatisticalEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                StatisticalEntity statusCV = StatisticalEntity.builder()
//                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
//                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
//                        .children((List<ChildrenStatusCVEntity>) doc.get(DbKeyConfig.CHILDREN))
                        .build();
                rows.add(statusCV);
            }
        }
        GetArrayStatisticalReponse<StatisticalEntity> resp = new GetArrayStatisticalReponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_PROFILE, cond));
        resp.setRows(rows);
        return resp;
    }
}
