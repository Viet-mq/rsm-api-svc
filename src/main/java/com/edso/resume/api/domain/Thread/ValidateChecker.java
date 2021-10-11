package com.edso.resume.api.domain.Thread;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;

import java.util.ArrayList;

public class ValidateChecker implements IChecker {

    private final ArrayList<Boolean> arrResult = new ArrayList<>();
    private final MongoDbOnlineSyncActions db;

    public ValidateChecker(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    public boolean validate(String idJobLevel, String idJob, String idSchool, String idSourceCV) {

        Thread t1 = new Thread(new JobLevelThread(db, this, idJobLevel));
        Thread t2 = new Thread(new JobThread(db, this, idJob));
        Thread t3 = new Thread(new SchoolThread(db, this, idSchool));
        Thread t4 = new Thread(new SourceCVThread(db, this, idSourceCV));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        // Thread main
        int countAll = 0;
        int countFalse = 0;

        while (countAll < 4 && countFalse < 1) {
            synchronized (arrResult) {
                for (Boolean b : arrResult) {
                    if (!b) {
                        countFalse++;
                    }
                    countAll++;
                }
                arrResult.clear();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return countFalse >= 1;
    }

    @Override
    public void onResult(boolean result) {
        synchronized (arrResult) {
            arrResult.add(result);
        }
    }
}
