package com.edso.resume.api.domain.validator;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.model.Filters;
import lombok.Data;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Data
public class GetTalentPoolProcessor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CountDownLatch countDownLatch;
    private final Document document;
    private final MongoDbOnlineSyncActions db;
    private final List<TalentPoolEntity> rows;

    public GetTalentPoolProcessor(CountDownLatch countDownLatch, Document document, MongoDbOnlineSyncActions db, List<TalentPoolEntity> rows) {
        this.countDownLatch = countDownLatch;
        this.document = document;
        this.db = db;
        this.rows = rows;
    }

    @Override
    public void run() {
        try {
//            List<Bson> c = new ArrayList<>();
//
//            Document query = new Document();
//            query.append("$unwind", "$talent_pool");
//
//            Document query1 = new Document();
//            Document match = new Document();
//            match.append("talent_pool.id", AppUtils.parseString(document.get("id")));
//            match.append("talent_pool.time", new Document().append("$gte", System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
//            query1.append("$match", match);
//
//            Document query2 = new Document();
//            query2.append("$group", new Document()
//                    .append("_id", null)
//                    .append("count", new Document().append("$sum", 1)
//                    )
//            );
//
//            c.add(query);
//            c.add(query1);
//            c.add(query2);
//
//            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);
//            long count = 0L;
//            if (lst.first() != null) {
//                count = AppUtils.parseLong(lst.first().get("count"));
//            }

            TalentPoolEntity talentPool = TalentPoolEntity.builder()
                    .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                    .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                    .managers((List<String>) document.get(DbKeyConfig.MANAGERS))
                    .description(AppUtils.parseString(document.get(DbKeyConfig.DESCRIPTION)))
                    .numberOfProfile(db.countAll(CollectionNameDefs.COLL_PROFILE, Filters.eq(DbKeyConfig.TALENT_POOL_ID, AppUtils.parseString(document.get(DbKeyConfig.ID)))))
                    .createAt(AppUtils.parseLong(document.get(DbKeyConfig.CREATE_AT)))
                    .createBy(AppUtils.parseString(document.get(DbKeyConfig.CREATE_BY)))
                    .total(db.countAll(CollectionNameDefs.COLL_PROFILE, Filters.and(Filters.eq(DbKeyConfig.TALENT_POOL_ID, AppUtils.parseString(document.get(DbKeyConfig.ID))), Filters.gte(DbKeyConfig.TALENT_POOL_TIME, System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000))))
                    .build();
            rows.add(talentPool);
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}
