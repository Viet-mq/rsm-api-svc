package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.request.CreateStatusCVRequest;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.response.BaseResponse;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StatusCVServiceImpl2 extends StatusCVServiceImpl {

    public StatusCVServiceImpl2(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public BaseResponse createStatusCV(CreateStatusCVRequest request, List<String> children) {
        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName();
            Bson c = Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            long count = db.countAll(CollectionNameDefs.COLL_STATUS_CV, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            List<Document> list = db.findAll(CollectionNameDefs.COLL_STATUS_CV, null, null, 0, 0);
            int index = 0;
            for (Document document : list) {
                if (!(Boolean) document.get(DbKeyConfig.DELETE) || (Boolean) document.get(DbKeyConfig.CHECK)) {
                    index = list.indexOf(document);
                }
            }

            Document statusCV = new Document();
            statusCV.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            statusCV.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            statusCV.append(DbKeyConfig.DELETE, false);
            statusCV.append(DbKeyConfig.CHECK, false);
            statusCV.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            statusCV.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            statusCV.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            statusCV.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            statusCV.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getMyOrganizations());

            list.add(index + 1, statusCV);

            // insert to database
            db.delete(CollectionNameDefs.COLL_STATUS_CV, null);
            db.insertMany(CollectionNameDefs.COLL_STATUS_CV, list);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
