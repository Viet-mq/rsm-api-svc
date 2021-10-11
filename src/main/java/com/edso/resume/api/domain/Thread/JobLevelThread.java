package com.edso.resume.api.domain.Thread;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class JobLevelThread implements Runnable {
    private final MongoDbOnlineSyncActions db;
    private final IChecker checker;
    private final String id;

    public JobLevelThread(MongoDbOnlineSyncActions db, IChecker checker, String id) {
        this.db = db;
        this.checker = checker;
        this.id = id;
    }

    @Override
    public void run() {
        Document jobLevel = db.findOne(CollectionNameDefs.COLL_JOB_LEVEL, Filters.eq(DbKeyConfig.ID, id));
        checker.onResult(jobLevel != null);

    }
}
