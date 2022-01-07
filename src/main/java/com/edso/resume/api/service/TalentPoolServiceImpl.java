package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.TalentPoolEntity;
import com.edso.resume.api.domain.request.CreateTalentPoolRequest;
import com.edso.resume.api.domain.request.DeleteTalentPoolRequest;
import com.edso.resume.api.domain.request.UpdateTalentPoolRequest;
import com.edso.resume.api.domain.validator.GetTalentPoolProcessor;
import com.edso.resume.api.domain.validator.IGetTalentPool;
import com.edso.resume.api.domain.validator.TalentPoolResult;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.github.slugify.Slugify;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

@Service
public class TalentPoolServiceImpl extends BaseService implements TalentPoolService, IGetTalentPool {

    private static final char[] SOURCE_CHARACTERS = {'À', 'Á', 'Â', 'Ã', 'È', 'É',
            'Ê', 'Ì', 'Í', 'Ò', 'Ó', 'Ô', 'Õ', 'Ù', 'Ú', 'Ý', 'à', 'á', 'â',
            'ã', 'è', 'é', 'ê', 'ì', 'í', 'ò', 'ó', 'ô', 'õ', 'ù', 'ú', 'ý',
            'Ă', 'ă', 'Đ', 'đ', 'Ĩ', 'ĩ', 'Ũ', 'ũ', 'Ơ', 'ơ', 'Ư', 'ư', 'Ạ',
            'ạ', 'Ả', 'ả', 'Ấ', 'ấ', 'Ầ', 'ầ', 'Ẩ', 'ẩ', 'Ẫ', 'ẫ', 'Ậ', 'ậ',
            'Ắ', 'ắ', 'Ằ', 'ằ', 'Ẳ', 'ẳ', 'Ẵ', 'ẵ', 'Ặ', 'ặ', 'Ẹ', 'ẹ', 'Ẻ',
            'ẻ', 'Ẽ', 'ẽ', 'Ế', 'ế', 'Ề', 'ề', 'Ể', 'ể', 'Ễ', 'ễ', 'Ệ', 'ệ',
            'Ỉ', 'ỉ', 'Ị', 'ị', 'Ọ', 'ọ', 'Ỏ', 'ỏ', 'Ố', 'ố', 'Ồ', 'ồ', 'Ổ',
            'ổ', 'Ỗ', 'ỗ', 'Ộ', 'ộ', 'Ớ', 'ớ', 'Ờ', 'ờ', 'Ở', 'ở', 'Ỡ', 'ỡ',
            'Ợ', 'ợ', 'Ụ', 'ụ', 'Ủ', 'ủ', 'Ứ', 'ứ', 'Ừ', 'ừ', 'Ử', 'ử', 'Ữ',
            'ữ', 'Ự', 'ự',};
    private static final char[] DESTINATION_CHARACTERS = {'A', 'A', 'A', 'A', 'E',
            'E', 'E', 'I', 'I', 'O', 'O', 'O', 'O', 'U', 'U', 'Y', 'a', 'a',
            'a', 'a', 'e', 'e', 'e', 'i', 'i', 'o', 'o', 'o', 'o', 'u', 'u',
            'y', 'A', 'a', 'D', 'd', 'I', 'i', 'U', 'u', 'O', 'o', 'U', 'u',
            'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A',
            'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'E', 'e',
            'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E',
            'e', 'I', 'i', 'I', 'i', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o',
            'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O',
            'o', 'O', 'o', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u',
            'U', 'u', 'U', 'u',};

    private final Queue<TalentPoolResult> queue = new LinkedBlockingQueue<>();

    public TalentPoolServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    public static char removeAccent(char ch) {
        int index = Arrays.binarySearch(SOURCE_CHARACTERS, ch);
        if (index >= 0) {
            ch = DESTINATION_CHARACTERS[index];
        }
        return ch;
    }

    public static String removeAccent(String s) {
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, removeAccent(sb.charAt(i)));
        }
        return sb.toString();
    }

    @Override
    public GetArrayResponse<TalentPoolEntity> findAll(HeaderInfo headerInfo, String id, String name, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        GetArrayResponse<TalentPoolEntity> resp = new GetArrayResponse<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        if (!Strings.isNullOrEmpty(id)) {
            c.add(Filters.eq("id", id));
        }
        Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
        Bson cond = buildCondition(c);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_TALENT_POOL, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<TalentPoolEntity> rows = new ArrayList<>();
        if (lst != null) {
            List<GetTalentPoolProcessor> rs = new ArrayList<>();
            String key = UUID.randomUUID().toString();
            for (Document doc : lst) {
                rs.add(new GetTalentPoolProcessor(key, doc, db, this));
            }
            int total = rs.size();

            for (GetTalentPoolProcessor r : rs) {
                Thread t = new Thread(r);
                t.start();
            }

            long time = System.currentTimeMillis();
            int count = 0;
            while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                TalentPoolResult result = queue.poll();
                if (result != null) {
                    if (result.getKey().equals(key)) {
                        if (!result.isResult()) {
                            resp.setFailed("Hệ thống bận!");
                            return resp;
                        } else {
                            count++;
                            rows.add(result.getTalentPool());
                        }
                        total--;
                    } else {
                        queue.offer(result);
                    }
                }
            }

            if (count != rs.size()) {
                for (GetTalentPoolProcessor r : rs) {
                    if (!r.getResult().isResult()) {
                        resp.setFailed("Hệ thống bận!");
                        return resp;
                    }
                }
            }
            Collections.sort(rows);
        }
        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_TALENT_POOL, cond));
        resp.setRows(rows);
        return resp;
    }

    @Override
    public BaseResponse createTalentPool(CreateTalentPoolRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            String name = request.getName().trim();
            Bson c = Filters.eq("name_search", name.toLowerCase());
            long count = db.countAll(CollectionNameDefs.COLL_TALENT_POOL, c);

            if (count > 0) {
                response.setFailed("Tên Talent Pool đã tồn tại!");
                return response;
            }

            //Check if manager is already in the system or not
            List<String> managers = request.getManagers();
            for (String manager : managers) {
                Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq("username", manager));
                if (user == null) {
                    response.setFailed("user " + manager + " không tồn tại");
                    return response;
                }
            }

            Slugify slugify = new Slugify();
            String nameAccentRemoved = removeAccent(name);
            Document talentPool = new Document();
            talentPool.append("id", slugify.slugify(nameAccentRemoved) + new Random().nextInt(10000));
            talentPool.append("name", name);
            talentPool.append("managers", request.getManagers());
            talentPool.append("description", request.getDescription());
            talentPool.append("numberOfProfile", 0);
            talentPool.append("name_search", name.toLowerCase());
            talentPool.append("create_at", System.currentTimeMillis());
            talentPool.append("update_at", System.currentTimeMillis());
            talentPool.append("create_by", request.getInfo().getUsername());
            talentPool.append("update_by", request.getInfo().getUsername());

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
            Bson cond = Filters.eq("id", id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            //Check if user have permission to update Talent Pool
            List<String> managers = parseList(idDocument.get("managers"));
            int check = 0;
            for (String manager : managers)
                if (manager.equals(request.getInfo().getUsername())) check = 1;
            if (check == 0) {
                response.setFailed("Người dùng không có quyền sửa Talent Pool");
                return response;
            }

            //Check if the name already exists or not
            String name = request.getName().trim();
            Document obj = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, Filters.eq("name_search", name.toLowerCase()));
            if (obj != null) {
                String objId = AppUtils.parseString(obj.get("id"));
                if (!objId.equals(id)) {
                    response.setFailed("Tên này đã tồn tại");
                    return response;
                }
            }

            //Check if manager is already in the system or not
            managers = request.getManagers();
            for (String manager : managers) {
                Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq("username", manager));
                if (user == null) {
                    response.setFailed("user " + manager + " không tồn tại");
                    return response;
                }
            }

            //update
            Bson updates = Updates.combine(
                    Updates.set("name", name),
                    Updates.set("name_search", name.toLowerCase()),
                    Updates.set("managers", request.getManagers()),
                    Updates.set("description", request.getDescription()),
                    Updates.set("numberOfProfile", request.getNumberOfProfile()),
                    Updates.set("update_at", System.currentTimeMillis()),
                    Updates.set("update_by", request.getInfo().getUsername())
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
            Bson cond = Filters.eq("id", id);
            Document idDocument = db.findOne(CollectionNameDefs.COLL_TALENT_POOL, cond);

            if (idDocument == null) {
                response.setFailed("Id này không tồn tại");
                return response;
            }

            //Check if user have permission to delete Talent Pool
            List<String> managers = parseList(idDocument.get("managers"));
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

    @Override
    public void onTalentPoolResult(TalentPoolResult result) {
        queue.offer(result);
    }
}
