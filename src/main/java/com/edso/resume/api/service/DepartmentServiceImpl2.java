package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.DepartmentEntity;
import com.edso.resume.api.domain.entities.SubDepartmentEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DepartmentServiceImpl2 extends DepartmentServiceImpl {
    public DepartmentServiceImpl2(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public GetArrayResponse<DepartmentEntity> findAll(HeaderInfo info, String name, Integer page, Integer size) {
        try {
            GetArrayResponse<DepartmentEntity> resp = new GetArrayResponse<>();
            List<Bson> c = new ArrayList<>();
            if (!Strings.isNullOrEmpty(name)) {
                c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(name))));
            }
            c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
            Bson cond = buildCondition(c);
            PagingInfo pagingInfo = PagingInfo.parse(page, size);
            List<Document> lst = db.findAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
            List<DepartmentEntity> rows = new ArrayList<>();
            if (!lst.isEmpty()) {
                while (!lst.isEmpty()) {
                    Document doc = lst.get(0);
                    String id = AppUtils.parseString(doc.get(DbKeyConfig.ID));
                    DepartmentEntity department = DepartmentEntity.builder()
                            .id(id)
                            .name(AppUtils.parseString(doc.get(DbKeyConfig.NAME)))
                            .build();
                    lst.remove(doc);
                    List<SubDepartmentEntity> listSub = new ArrayList<>();
                    List<Document> removes = new ArrayList<>();
                    for (Document document : lst) {
                        if (id.equals(AppUtils.parseString(document.get(DbKeyConfig.PARENT_ID)))) {
                            SubDepartmentEntity subDepartmentEntity = SubDepartmentEntity.builder()
                                    .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                                    .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                                    .build();
                            listSub.add(subDepartmentEntity);
                            removes.add(document);
                        }
                    }
                    for (Document document : removes) {
                        lst.remove(document);
                    }
                    if (!listSub.isEmpty()) {
                        department.setChildren(listSub);
                    }
                    a(listSub, lst);
                    rows.add(department);
                }
            }
            resp.setSuccess();
            resp.setTotal(db.countAll(CollectionNameDefs.COLL_DEPARTMENT_COMPANY, null));
            Collections.reverse(rows);
            resp.setRows(rows);
            return resp;
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            return null;
        }
    }


    private void a(List<SubDepartmentEntity> listSub, List<Document> lst) {
        if (listSub != null) {
            for (SubDepartmentEntity sub : listSub) {
                List<SubDepartmentEntity> subs = new ArrayList<>();
                List<Document> listRemove = new ArrayList<>();
                for (Document document : lst) {
                    if (sub.getId().equals(AppUtils.parseString(document.get(DbKeyConfig.PARENT_ID)))) {
                        SubDepartmentEntity subDepartmentEntity = SubDepartmentEntity.builder()
                                .id(AppUtils.parseString(document.get(DbKeyConfig.ID)))
                                .name(AppUtils.parseString(document.get(DbKeyConfig.NAME)))
                                .build();
                        subs.add(subDepartmentEntity);
                        listRemove.add(document);
                    }
                }
                for (Document document : listRemove) {
                    lst.remove(document);
                }
                if (!subs.isEmpty()) {
                    sub.setChildren(subs);
                }
                a(subs, lst);
            }
        }
    }
}
