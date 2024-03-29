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
            c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
        }
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
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
            String name = request.getName();
            Bson c = Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            long count = db.countAll(CollectionNameDefs.COLL_ADDRESS, c);

            if (count > 0) {
                response.setFailed("Tên này đã tồn tại");
                return response;
            }

            Document address = new Document();
            address.append(DbKeyConfig.ID, UUID.randomUUID().toString());
            address.append(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name));
            address.append(DbKeyConfig.OFFICE_NAME, AppUtils.mergeWhitespace(request.getOfficeName()));
            address.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name));
            address.append(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase()));
            address.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
            address.append(DbKeyConfig.UPDATE_AT, System.currentTimeMillis());
            address.append(DbKeyConfig.CREATE_BY, request.getInfo().getUsername());
            address.append(DbKeyConfig.UPDATE_BY, request.getInfo().getUsername());
            address.append(DbKeyConfig.ORGANIZATIONS, request.getInfo().getMyOrganizations());

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
            Bson cond = Filters.and(Filters.eq(DbKeyConfig.ID, id), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ADDRESS, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            String name = request.getName();
            Document obj = db.findOne(CollectionNameDefs.COLL_ADDRESS, Filters.and(Filters.eq(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations())));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get(DbKeyConfig.ID));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            Bson idAddress = Filters.eq(DbKeyConfig.ADDRESS_ID, request.getId());
            Bson updateRecruitment = Updates.combine(
                    Updates.set(DbKeyConfig.ADDRESS_NAME, AppUtils.mergeWhitespace(request.getName()))
            );
            db.update(CollectionNameDefs.COLL_RECRUITMENT, idAddress, updateRecruitment);

            Bson address = Filters.eq(DbKeyConfig.INTERVIEW_ADDRESS_ID, request.getId());
            Bson updateCalendar = Updates.combine(
                    Updates.set(DbKeyConfig.INTERVIEW_ADDRESS_NAME, AppUtils.mergeWhitespace(request.getName()))
            );
            db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, address, updateCalendar);


            // update roles
            Bson updates = Updates.combine(
                    Updates.set(DbKeyConfig.NAME, AppUtils.mergeWhitespace(name)),
                    Updates.set(DbKeyConfig.OFFICE_NAME, AppUtils.mergeWhitespace(request.getOfficeName())),
                    Updates.set(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(name)),
                    Updates.set(DbKeyConfig.NAME_EQUAL, AppUtils.mergeWhitespace(name.toLowerCase())),
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
            if (recruitment != null) {
                response.setFailed("Không thể xóa địa chỉ này!");
                return response;
            }
            Document calendar = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, Filters.eq(DbKeyConfig.INTERVIEW_ADDRESS_ID, request.getId()));
            if (calendar != null) {
                response.setFailed("Không thể xóa địa chỉ này!");
                return response;
            }
            String id = request.getId();
            Bson cond = Filters.and(Filters.eq(DbKeyConfig.ID, id), Filters.in(DbKeyConfig.ORGANIZATIONS, request.getInfo().getOrganizations()));
            Document idDocument = db.findOne(CollectionNameDefs.COLL_ADDRESS, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }
            db.delete(CollectionNameDefs.COLL_ADDRESS, cond);
            response.setSuccess();
            return response;
        } catch (Throwable ex) {

            logger.error("Exception: ", ex);
            response.setFailed("Hệ thống đang bận");
            return response;

        }
    }
}
