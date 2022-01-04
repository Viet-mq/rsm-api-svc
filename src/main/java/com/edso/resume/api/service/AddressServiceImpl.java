package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.AddressEntity;
import com.edso.resume.api.domain.request.CreateAddressRequest;
import com.edso.resume.api.domain.request.DeleteAddressRequest;
import com.edso.resume.api.domain.request.UpdateAddressRequest;
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
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AddressServiceImpl extends BaseService implements AddressService {

    protected AddressServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<AddressEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(name.toLowerCase())));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_ADDRESS, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<AddressEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                AddressEntity address = AddressEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .officeName(AppUtils.parseString(doc.get(DbKeyConfig.OFFICE_NAME)))
                        .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                        .build();
                rows.add(address);
            }
        }
        GetArrayResponse<AddressEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_ADDRESS, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createAddress(CreateAddressRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName().trim();
            Bson c = Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            long count = db.countAll(CollectionNameDefs.COLL_ADDRESS, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document address = new Document();
            address.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            address.append(DbKeyConfig.NAME, name);
            address.append(DbKeyConfig.OFFICE_NAME, request.getOfficeName());
            address.append(DbKeyConfig.NAME_SEARCH, name.toLowerCase());
            address.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            address.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            address.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            address.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_ADDRESS, address);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse updateAddress(UpdateAddressRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String id = request.getId();
            Bson cond = Filters.eq(DbKeyConfig.ID, id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ADDRESS, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_ADDRESS, Filters.eq(DbKeyConfig.NAME_SEARCH, name.toLowerCase()));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idAddress = Filters.eq(DbKeyConfig.ADDRESS_ID, request.getId());
            Bson updateRecruitment = Updates.combine(
                    Updates.set(DbKeyConfig.ADDRESS_NAME, request.getName())
            );
            db.update(CollectionNameDefs.COLL_RECRUITMENT, idAddress, updateRecruitment, true);
            db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, idAddress, updateRecruitment, true);


            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, name),
                    Updates.set(DbKeyConfig.OFFICE_NAME, request.getOfficeName()),
                    Updates.set(DbKeyConfig.NAME_SEARCH, name.toLowerCase()),
                    Updates.set(DbKeyConfig.UPDATE_AT, System.currentTimeMillis()),
                    Updates.set(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername())
            );
            db.update(CollectionNameDefs.COLL_ADDRESS, cond, updates, true);
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
        response.setSuccess();
        return response;
    }

    @Override
    public BaseResponse deleteAddress(DeleteAddressRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            Document recruitment = db.findOne(CollectionNameDefs.COLL_RECRUITMENT, Filters.eq(DbKeyConfig.ADDRESS_ID, request.getId()));
            Document calendar = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, Filters.eq(DbKeyConfig.ADDRESS_ID, request.getId()));
            if (recruitment == null && calendar == null) {
                String id = request.getId();
                Bson cond = Filters.eq(DbKeyConfig.ID, id);
                Document idDocument = db.findOne(CollectionNameDefs.COLL_ADDRESS, cond);

                if (idDocument == null) {
                    response.setFailed("Id này không tồn tại");
                    return response;
                }
                db.delete(CollectionNameDefs.COLL_ADDRESS, cond);
                response.setSuccess();
                return response;
            } else {
                response.setFailed("Không thể xóa địa chỉ này!");
                return response;
            }
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
