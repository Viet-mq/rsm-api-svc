package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.UrlConsumerEntity;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UrlConsumerServiceImpl extends BaseService implements UrlConsumerService{

    private final MongoDbOnlineSyncActions db;

    protected UrlConsumerServiceImpl(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate) {
        super(db, rabbitTemplate);
        this.db = db;
    }

    @Override
    public void insertUrlToProfile(UrlConsumerEntity url) {

        Bson cond = Filters.eq(DbKeyConfig.ID, url.getId());
        Document idProfile = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if(idProfile == null) return;

        Bson update = Updates.combine(
                Updates.set(DbKeyConfig.URL_CV, url.getUrl()),
                Updates.set(DbKeyConfig.CV_TYPE, url.getType()),
                Updates.set(DbKeyConfig.CV, url.getFileName())
        );

        db.update(CollectionNameDefs.COLL_PROFILE, cond, update, true);
        logger.info("Insert url to profile with id: {}", url.getId());
    }
}
