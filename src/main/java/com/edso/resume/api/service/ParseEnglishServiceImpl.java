package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

@Service
public class ParseEnglishServiceImpl extends BaseService implements ParseEnglishService {
    protected ParseEnglishServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public void parseEnglish() {
        FindIterable<Document> list = db.findAll2(CollectionNameDefs.COLL_RECRUITMENT, null, null, 0, 0);
        if (list != null) {
            for (Document document : list) {
                Bson cond = Filters.eq(DbKeyConfig.ID, AppUtils.parseString(document.get(DbKeyConfig.ID)));
                Bson update = Updates.combine(
                        Updates.set(DbKeyConfig.NAME_SEARCH, parseVietnameseToEnglish(AppUtils.parseString(document.get(DbKeyConfig.TITLE))))
                );
                db.update(CollectionNameDefs.COLL_RECRUITMENT, cond, update, true);
            }
        }
    }
}
