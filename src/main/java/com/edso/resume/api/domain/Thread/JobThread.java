package com.edso.resume.api.domain.Thread;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class JobThread implements Runnable {

    private final MongoDbOnlineSyncActions db;
    private final IChecker checker;
    private String id;

    public JobThread(MongoDbOnlineSyncActions db, IChecker checker, String id) {
        this.db = db;
        this.checker = checker;
        this.id = id;
    }

    @Override
    public void run() {
        boolean result = false;

        try{
            Document job = db.findOne(CollectionNameDefs.COLL_JOB, Filters.eq(DbKeyConfig.ID, id));
            if (job != null) {
                result = true;
            } else {
                result = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            checker.onResult(result);
        }

    }
}
