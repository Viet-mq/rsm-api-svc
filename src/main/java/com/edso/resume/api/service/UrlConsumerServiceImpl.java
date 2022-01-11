package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.UrlConsumerEntity;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

@Service
public class UrlConsumerServiceImpl extends BaseService implements UrlConsumerService {

    protected UrlConsumerServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public void updateUrlToProfile(UrlConsumerEntity url) {
        try {
            Bson cond = Filters.eq(DbKeyConfig.ID, url.getId());
            Document idProfile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);
            logger.info("=>updateUrlToProfile url: {}", url);
            if (idProfile == null) {
                logger.info("Id profile không tồn tại");
                return;
            }

            Bson update = Updates.combine(
                    Updates.set(DbKeyConfig.URL_CV, url.getUrl()),
                    Updates.set(DbKeyConfig.CV, url.getFileName())
            );

            db.update(CollectionNameDefs.COLL_PROFILE, cond, update, true);
            logger.info("<=updateUrlToProfile url: {}", url);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }
}
