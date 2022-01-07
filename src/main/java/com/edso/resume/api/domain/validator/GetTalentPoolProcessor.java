package com.edso.resume.api.domain.validator;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.AggregateIterable;
import lombok.Data;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetTalentPoolProcessor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IGetTalentPool target;
    private final String key;
    private final Document document;
    private final MongoDbOnlineSyncActions db;
    private TalentPoolResult result;

    public GetTalentPoolProcessor(String key, Document document, MongoDbOnlineSyncActions db, IGetTalentPool target) {
        this.target = target;
        this.key = key;
        this.document = document;
        this.db = db;
        this.result = new TalentPoolResult(key);

    }

    @Override
    public void run() {
        try {
            List<Bson> c = new ArrayList<>();

            Document query = new Document();
            query.append("$unwind", "$talent_pool");

            Document query1 = new Document();
            Document match = new Document();
            match.append("talent_pool.id", AppUtils.parseString(document.get("id")));
            match.append("talent_pool.time", new Document().append("$gte", System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
            query1.append("$match", match);

            Document query2 = new Document();
            query2.append("$group", new Document()
                    .append("_id", null)
                    .append("count", new Document().append("$sum", 1)
                    )
            );

            c.add(query);
            c.add(query1);
            c.add(query2);

            AggregateIterable<Document> lst = db.countGroupBy(CollectionNameDefs.COLL_PROFILE, c);
            long count = 0L;
            if (lst.first() != null) {
                count = AppUtils.parseLong(lst.first().get("count"));
            }

            TalentPoolEntity talentPool = TalentPoolEntity.builder()
                    .id(AppUtils.parseString(document.get("id")))
                    .name(AppUtils.parseString(document.get("name")))
                    .managers((List<String>) document.get("managers"))
                    .description(AppUtils.parseString(document.get("description")))
                    .numberOfProfile(AppUtils.parseInt(document.get("numberOfProfile")))
                    .createAt(AppUtils.parseLong(document.get(DbKeyConfig.CREATE_AT)))
                    .createBy(AppUtils.parseString(document.get(DbKeyConfig.CREATE_BY)))
                    .total(count)
                    .build();
            result.setResult(true);
            result.setTalentPool(talentPool);
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            result.setResult(false);
        } finally {
            target.onTalentPoolResult(result);
        }
    }
}
