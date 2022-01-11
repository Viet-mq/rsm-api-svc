package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.api.domain.request.CreateTalentPoolRequest;
import com.edso.resume.api.domain.request.DeleteTalentPoolRequest;
import com.edso.resume.api.domain.request.UpdateTalentPoolRequest;
import com.edso.resume.api.domain.validator.GetTalentPoolProcessor;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

@Service
public class TalentPoolServiceImpl extends BaseService implements TalentPoolService {

    public TalentPoolServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<TalentPoolEntity> findAll(HeaderInfo headerInfo, String id, String name, Integer page, Integer size) {
        GetArrayResponse<TalentPoolEntity> resp = new GetArrayResponse<>();
        try {
            List<Bson> c = new ArrayList<>();
            if (!Strings.isNullOrEmpty(name)) {
                c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
            }
            if (!Strings.isNullOrEmpty(id)) {
                c.add(Filters.eq(DbKeyConfig.ID, id));
            }
            Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
            Bson cond = buildCondition(c);
            PagingInfo pagingInfo = PagingInfo.parse(page, size);
            FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_TALENT_POOL, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
            List<TalentPoolEntity> rows = new ArrayList<>();
            if (lst != null) {
                int count = 0;
                for (Document ignored : lst) {
                    count++;
                }

                CountDownLatch countDownLatch = new CountDownLatch(count);

                for (Document doc : lst) {
                    Thread t = new Thread(new GetTalentPoolProcessor(countDownLatch, doc, db, rows));
                    t.start();
                }

                countDownLatch.await();

                Collections.sort(rows);
            }
            resp.setSuccess();
            resp.setTotal(db.countAll(CollectionNameDefs.COLL_TALENT_POOL, cond));
            resp.setRows(rows);
            return resp;
        } catch (Throwable ex) {
            resp.setFailed("Hệ thống bận!");
            return resp;
        }
    }

    @Override
    public BaseResponse createTalentPool(CreateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName().trim();
            Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            long count = db.countAll(CollectionNameDefs.COLL_TALENT_POOL, c);

            if (count > 0) {
                response.setFailed("Tên Talent Pool đã tồn tại!");
                return response;
            }

            //Check if manager is already in the system or not
            List<String> managers = request.getManagers();
            for (String manager : managers) {
                Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, manager));
                if (user == null) {
                    response.setFailed("user " + manager + " không tồn tại");
                    return response;
                }
            }

            Document talentPool = new Document();
            talentPool.append(DbKeyConfig.ID, AppUtils.slugify(name));
            talentPool.append(DbKeyConfig.NAME, name);
            talentPool.append(DbKeyConfig.MANAGERS, request.getManagers());
            talentPool.append(DbKeyConfig.DESCRIPTION, request.getDescription());
            talentPool.append(DbKeyConfig.NUMBER_OF_PROFILE, 0);
            talentPool.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            talentPool.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            talentPool.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            talentPool.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            talentPool.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            db.insertOne(CollectionNameDefs.COLL_TALENT_POOL, talentPool);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateTalentPool(UpdateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            //Check if user have permission to update Talent Pool
            List<String> managers = (List<String>) idDocument.get(DbKeyConfig.MANAGERS);
            int check = 0;
            for (String manager : managers)
                if (manager.equals(request.getInfo().getUsername())) check = 1;
            if (check == 0) {
                response.setFailed("Người dùng không có quyền sửa Talent Pool");
                return response;
            }

            //Check if the name already exists or not
            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            //Check if manager is already in the system or not
            managers = request.getManagers();
            for (String manager : managers) {
                Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, manager));
                if (user == null) {
                    response.setFailed("user " + manager + " không tồn tại");
                    return response;
                }
            }

            //update
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, name),
                    Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                    Updates.set(DbKeyConfig.MANAGERS, request.getManagers()),
                    Updates.set(DbKeyConfig.DESCRIPTION, request.getDescription()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_TALENT_POOL, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteTalentPool(DeleteTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            //Check if user have permission to delete Talent Pool
            List<String> managers = (List<String>) idDocument.get(DbKeyConfig.MANAGERS);
            int check = 0;
            for (String manager : managers) {
                if (manager.equals(request.getInfo().getUsername())) check = 1;
            }

            if (check == 0) {
                response.setFailed("Người dùng không có quyền xóa Talent Pool");
                return response;
            }

            //delete
            db.delete(CollectionNameDefs.COLL_TALENT_POOL, cond);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        return new BaseResponse(0, "OK");
    }

}
