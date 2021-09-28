package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarReponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProfileServiceImpl extends BaseService implements ProfileService {

    private final MongoDbOnlineSyncActions db;
    public ProfileServiceImpl(MongoDbOnlineSyncActions db) {
        this.db = db;
    }

    @Override
    public GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String fullName, String idProfile, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fullName)) {
            c.add(Filters.regex("name_search", Pattern.compile(fullName.toLowerCase())));
        }
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("id", Pattern.compile(idProfile)));
            //Insert history to DB
            createHistory(idProfile,"Select", info.getUsername());
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<ProfileEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ProfileEntity profile = ProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .fullName(AppUtils.parseString(doc.get("fullName")))
                        .dateOfBirth(AppUtils.parseString(doc.get("dateOfBirth")))
                        .hometown(AppUtils.parseString(doc.get("hometown")))
                        .school(AppUtils.parseString(doc.get("school")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .job(AppUtils.parseString(doc.get("job")))
                        .levelJob(AppUtils.parseString(doc.get("levelJob")))
                        .cv(AppUtils.parseString(doc.get("cv")))
                        .sourceCV(AppUtils.parseString(doc.get("sourceCV")))
                        .hrRef(AppUtils.parseString(doc.get("hrRef")))
                        .dateOfApply(AppUtils.parseString(doc.get("dateOfApply")))
                        .cvType(AppUtils.parseString(doc.get("cvType")))
                        .statusCV(AppUtils.parseString(doc.get("statusCV")))
                        .lastApply(AppUtils.parseString(doc.get("lastApply")))
                        .tags(AppUtils.parseString(doc.get("tags")))
                        .gender(AppUtils.parseString(doc.get("gender")))
                        .note(AppUtils.parseString(doc.get("note")))
                        .dateOfCreate(parseDate(AppUtils.parseLong(doc.get("create_at"))))
                        .dateOfUpdate(parseDate(AppUtils.parseLong(doc.get("update_at"))))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
                        .build();
                rows.add(profile);
            }
        }

        GetArrayResponse<ProfileEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public GetArrayResponse<HistoryEntity> findAllHistory(HeaderInfo info, String idProfile, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_HISTORY_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_HISTORY_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<HistoryEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                HistoryEntity history = HistoryEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .time(parseDate(AppUtils.parseLong(doc.get("time"))))
                        .action(AppUtils.parseString(doc.get("action")))
                        .by(AppUtils.parseString(doc.get("by")))
                        .build();
                rows.add(history);
            }
        }
        GetArrayResponse<HistoryEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public GetArrayResponse<NoteProfileEntity> findAllNote(HeaderInfo info, String idProfile, Integer page, Integer size) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        long total = db.countAll(CollectionNameDefs.COLL_NOTE_PROFILE, cond);
        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_NOTE_PROFILE, cond, null, pagingInfo.getStart(), pagingInfo.getLimit());
        List<NoteProfileEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                NoteProfileEntity noteProfile = NoteProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .note(AppUtils.parseString(doc.get("note")))
                        .create_at(parseDate(AppUtils.parseLong(doc.get("create_at"))))
                        .create_by(AppUtils.parseString(doc.get("create_by")))
                        .build();
                rows.add(noteProfile);
            }
        }
        GetArrayResponse<NoteProfileEntity> resp = new GetArrayResponse<>();
        resp.setSuccess();
        resp.setTotal(total);
        resp.setRows(rows);
        return resp;
    }

    @Override
    public GetArrayCalendarReponse<CalendarEntity> findAllCalendar(HeaderInfo info, String idProfile) {
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(idProfile)) {
            c.add(Filters.regex("idProfile", Pattern.compile(idProfile)));
        }
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, null, 0, 0);
        List<CalendarEntity> calendars = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                CalendarEntity calendar = CalendarEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .idProfile(AppUtils.parseString(doc.get("idProfile")))
                        .time(AppUtils.parseString(doc.get("time")))
                        .address(AppUtils.parseString(doc.get("address")))
                        .form(AppUtils.parseString(doc.get("form")))
                        .interviewer(parseList(doc.get("interviewer")))
                        .interviewee(AppUtils.parseString(doc.get("interviewee")))
                        .content(AppUtils.parseString(doc.get("content")))
                        .question(parseList(doc.get("question")))
                        .comment(parseList(doc.get("comment")))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
                        .status(AppUtils.parseString(doc.get("status")))
                        .reason(AppUtils.parseString(doc.get("reason")))
                        .timeStart(AppUtils.parseString(doc.get("timeStart")))
                        .timeFinish(AppUtils.parseString(doc.get("timeFinish")))
                        .build();
                calendars.add(calendar);
            }
        }
        GetArrayCalendarReponse<CalendarEntity> resp = new GetArrayCalendarReponse<>();
        resp.setSuccess();
        resp.setCalendars(calendars);
        return resp;
    }

    @SuppressWarnings (value="unchecked")
    public List<String> parseList(Object list){
        return (List<String>) list;
    }

    public void createHistory(String idProfile, String action, String by)  {

        Document history = new Document();
        history.append("id", UUID.randomUUID().toString());
        history.append("idProfile", idProfile);
        history.append("time", System.currentTimeMillis());
        history.append("action", action);
        history.append("by", by);

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_HISTORY_PROFILE, history);
    }

    @Override
    public BaseResponse createProfile(CreateProfileRequest request)  {

        BaseResponse response = new BaseResponse();

        String idProfile = UUID.randomUUID().toString();

        Document profile = new Document();
        profile.append("id", idProfile);
        profile.append("fullName", request.getFullName());
        profile.append("dateOfBirth", request.getDateOfBirth());
        profile.append("hometown", request.getHometown());
        profile.append("school", request.getSchool());
        profile.append("phoneNumber", request.getPhoneNumber());
        profile.append("email", request.getEmail());
        profile.append("job", request.getJob());
        profile.append("levelJob", request.getLevelJob());
        profile.append("cv", request.getCv());
        profile.append("sourceCV", request.getSourceCV());
        profile.append("hrRef", request.getHrRef());
        profile.append("dateOfApply", request.getDateOfApply());
        profile.append("cvType", request.getCvType());
        profile.append("name_search", request.getFullName().toLowerCase());
        profile.append("create_at", System.currentTimeMillis());
        profile.append("update_at", System.currentTimeMillis());
        profile.append("update_statuscv_at", System.currentTimeMillis());
        profile.append("create_by", request.getInfo().getUsername());
        profile.append("update_by", request.getInfo().getUsername());
        profile.append("update_statuscv_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_PROFILE, profile);

        //Insert history to DB
        createHistory(idProfile,"Create",request.getInfo().getUsername());

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse createNoteProfile(CreateNoteProfileRequest request)  {

        BaseResponse response = new BaseResponse();

        Document profile = new Document();
        profile.append("id", UUID.randomUUID().toString());
        profile.append("idProfile", request.getIdProfile());
        profile.append("note", request.getNote());
        profile.append("create_at", System.currentTimeMillis());
        profile.append("create_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_NOTE_PROFILE, profile);

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse createCalendarProfile(CreateCalendarProfileRequest request)  {

        BaseResponse response = new BaseResponse();

        String idProfile = request.getIdProfile();

        Document profile = new Document();
        profile.append("id", UUID.randomUUID().toString());
        profile.append("idProfile", idProfile);
        profile.append("time", request.getTime());
        profile.append("address", request.getAddress());
        profile.append("form", request.getForm());
        profile.append("interviewer", request.getInterviewer());
        profile.append("interviewee", request.getInterviewee());
        profile.append("content", request.getContent());
        profile.append("question", request.getQuestion());
        profile.append("comment", request.getComment());
        profile.append("evaluation", request.getEvaluation());
        profile.append("status", request.getStatus());
        profile.append("reason", request.getReason());
        profile.append("timeStart", request.getTimeStart());
        profile.append("timeFinish", request.getTimeFinish());
        profile.append("create_at", System.currentTimeMillis());
        profile.append("update_at", System.currentTimeMillis());
        profile.append("create_by", request.getInfo().getUsername());
        profile.append("update_by", request.getInfo().getUsername());

        // insert to database
        db.insertOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, profile);

        //Insert history to DB
        createHistory(idProfile,"Create calendar",request.getInfo().getUsername());

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse updateProfile(UpdateProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("fullName", request.getFullName()),
                Updates.set("dateOfBirth", request.getDateOfBirth()),
                Updates.set("hometown", request.getHometown()),
                Updates.set("school", request.getSchool()),
                Updates.set("phoneNumber", request.getPhoneNumber()),
                Updates.set("email", request.getEmail()),
                Updates.set("job", request.getJob()),
                Updates.set("levelJob", request.getLevelJob()),
                Updates.set("cv", request.getCv()),
                Updates.set("sourceCV", request.getSourceCV()),
                Updates.set("hrRef", request.getHrRef()),
                Updates.set("dateOfApply", request.getDateOfApply()),
                Updates.set("cvType", request.getCvType()),
                Updates.set("tags", request.getTags()),
                Updates.set("note", request.getNote()),
                Updates.set("gender", request.getGender()),
                Updates.set("lastApply", request.getLastApply()),
                Updates.set("evaluation", request.getEvaluation()),
                Updates.set("name_search", request.getFullName().toLowerCase()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername()),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

        //Insert history to DB
        createHistory(id,"Update",request.getInfo().getUsername());

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse updateCalendarProfile(UpdateCalendarProfileRequest request) {

        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("time", request.getTime()),
                Updates.set("address", request.getAddress()),
                Updates.set("form", request.getForm()),
                Updates.set("interviewer", request.getInterviewer()),
                Updates.set("interviewee", request.getInterviewee()),
                Updates.set("content", request.getContent()),
                Updates.set("question", request.getQuestion()),
                Updates.set("comment", request.getComment()),
                Updates.set("evaluation", request.getEvaluation()),
                Updates.set("status", request.getStatus()),
                Updates.set("reason", request.getReason()),
                Updates.set("timeStart", request.getTimeStart()),
                Updates.set("timeFinish", request.getTimeFinish()),
                Updates.set("update_at", System.currentTimeMillis()),
                Updates.set("update_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond, updates, true);

        //Insert history to DB
        createHistory(id,"Update calendar",request.getInfo().getUsername());

        response.setSuccess();
        return response;

    }

    @Override
    public BaseResponse deleteProfile(DeleteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_PROFILE, cond);

        //Insert history to DB
        createHistory(id,"Delete",request.getInfo().getUsername());

        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse deleteCalendarProfile(DeleteCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        db.delete(CollectionNameDefs.COLL_CALENDAR_PROFILE, cond);

        //Insert history to DB
        createHistory(id,"Delete calendar",request.getInfo().getUsername());

        return new BaseResponse(0, "OK");
    }

    @Override
    public BaseResponse updateStatusProfile(UpdateStatusProfileRequest request) {
        BaseResponse response = new BaseResponse();
        String id = request.getId();
        Bson cond = Filters.eq("id", id);
        Document idDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, cond);

        if (idDocument == null) {
            response.setFailed("Id này không tồn tại");
            return response;
        }

        // update roles
        Bson updates = Updates.combine(
                Updates.set("statusCV", request.getStatusCV()),
                Updates.set("update_statuscv_at", System.currentTimeMillis()),
                Updates.set("update_statuscv_by", request.getInfo().getUsername())
        );
        db.update(CollectionNameDefs.COLL_PROFILE, cond, updates, true);

        //Insert history to DB
        createHistory(id,"Update status",request.getInfo().getUsername());

        response.setSuccess();
        return response;
    }


    private static final int COLUMN_INDEX_FULLNAME = 0;
    private static final int COLUMN_INDEX_GENDER = 1;
    private static final int COLUMN_INDEX_PHONENUMBER = 2;
    private static final int COLUMN_INDEX_EMAIL = 3;
    private static final int COLUMN_INDEX_DATEOFBIRTH = 4;
    private static final int COLUMN_INDEX_HOMETOWN = 5;
    private static final int COLUMN_INDEX_SCHOOL = 6;
    private static final int COLUMN_INDEX_JOB = 7;
    private static final int COLUMN_INDEX_LEVELJOB = 8;
    private static final int COLUMN_INDEX_CV = 9;
    private static final int COLUMN_INDEX_SOURCECV = 10;
    private static final int COLUMN_INDEX_HRREF = 11;
    private static final int COLUMN_INDEX_DATEOFAPPLY = 12;
    private static final int COLUMN_INDEX_CVTYPE = 13;
    private static final int COLUMN_INDEX_LASTAPPLY = 14;
    private static final int COLUMN_INDEX_TAGS = 15;
    private static final int COLUMN_INDEX_DATEOFCREATE = 16;
    private static final int COLUMN_INDEX_DATEOFUPDATE = 17;
    private static final int COLUMN_INDEX_NOTE = 18;
    private static final int COLUMN_INDEX_EVALUATION = 19;
    private static final int COLUMN_INDEX_STATUSCV = 20;

    @Override
    public byte[] exportExcel(HeaderInfo info, String name) throws IOException {
        final String excelFilePath = "D:\\Profiles.xlsx";
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, null, 0, 0);
        List<ProfileEntity> profiles = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                ProfileEntity profile = ProfileEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .fullName(AppUtils.parseString(doc.get("fullName")))
                        .dateOfBirth(AppUtils.parseString(doc.get("dateOfBirth")))
                        .hometown(AppUtils.parseString(doc.get("hometown")))
                        .school(AppUtils.parseString(doc.get("school")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .job(AppUtils.parseString(doc.get("job")))
                        .levelJob(AppUtils.parseString(doc.get("levelJob")))
                        .cv(AppUtils.parseString(doc.get("cv")))
                        .sourceCV(AppUtils.parseString(doc.get("sourceCV")))
                        .hrRef(AppUtils.parseString(doc.get("hrRef")))
                        .dateOfApply(AppUtils.parseString(doc.get("dateOfApply")))
                        .cvType(AppUtils.parseString(doc.get("cvType")))
                        .statusCV(AppUtils.parseString(doc.get("statusCV")))
                        .lastApply(AppUtils.parseString(doc.get("lastApply")))
                        .tags(AppUtils.parseString(doc.get("tags")))
                        .gender(AppUtils.parseString(doc.get("gender")))
                        .note(AppUtils.parseString(doc.get("note")))
                        .dateOfCreate(parseDate(AppUtils.parseLong(doc.get("create_at"))))
                        .dateOfUpdate(parseDate(AppUtils.parseLong(doc.get("update_at"))))
                        .evaluation(AppUtils.parseString(doc.get("evaluation")))
                        .build();
                profiles.add(profile);
            }
        }
        return writeExcel(profiles, excelFilePath);
    }

    public String parseDate(Long milliSeconds){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static byte[] writeExcel(List<ProfileEntity> profiles, String excelFilePath) throws IOException {
        // Create Workbook
        Workbook workbook = getWorkbook(excelFilePath);

        // Create sheet
        Sheet sheet = workbook.createSheet("Profiles"); // Create sheet with sheet name

        int rowIndex = 0;

        // Write header
        writeHeader(sheet, rowIndex);

        // Write data
        rowIndex++;
        for (ProfileEntity profile : profiles) {
            // Create row
            Row row = sheet.createRow(rowIndex);
            // Write data on row
            writeBook(profile, row);
            rowIndex++;
        }

        // Create file excel
        createOutputFile(workbook, excelFilePath);

        return openFile(excelFilePath);
    }

    private static byte[] openFile(String excelFilePath) throws IOException {
        RandomAccessFile f = new RandomAccessFile(excelFilePath, "r");
        byte[] b = new byte[(int) f.length()];
        f.readFully(b);
        f.close();
        return b;
    }

    // Create workbook
    private static Workbook getWorkbook(String excelFilePath) {
        Workbook workbook ;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }

    // Write header with format
    private static void writeHeader(Sheet sheet, int rowIndex) {
        // create CellStyle
        CellStyle cellStyle = createStyleForHeader(sheet);

        // Create row
        Row row = sheet.createRow(rowIndex);

        // Create cells
        Cell cell = row.createCell(COLUMN_INDEX_FULLNAME);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("HỌ TÊN");

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ĐỊA CHỈ EMAIL");

        cell = row.createCell(COLUMN_INDEX_PHONENUMBER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("SỐ ĐIỆN THOẠI");

        cell = row.createCell(COLUMN_INDEX_JOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("JOB TITLE");

        cell = row.createCell(COLUMN_INDEX_LASTAPPLY);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ỨNG TUYỂN GẦN NHẤT");

        cell = row.createCell(COLUMN_INDEX_SOURCECV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("NGUỒN");

        cell = row.createCell(COLUMN_INDEX_TAGS);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TAGS");

        cell = row.createCell(COLUMN_INDEX_DATEOFBIRTH);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("NGÀY SINH");

        cell = row.createCell(COLUMN_INDEX_GENDER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("GIỚI TÍNH");

        cell = row.createCell(COLUMN_INDEX_HOMETOWN);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ĐỊA CHỈ");

        cell = row.createCell(COLUMN_INDEX_NOTE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("GHI CHÚ");

        cell = row.createCell(COLUMN_INDEX_DATEOFCREATE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TG TẠO");

        cell = row.createCell(COLUMN_INDEX_DATEOFAPPLY);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TG ỨNG TUYỂN");

        cell = row.createCell(COLUMN_INDEX_DATEOFUPDATE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TG CẬP NHẬT");

        cell = row.createCell(COLUMN_INDEX_EVALUATION);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ĐÁNH GIÁ");

        cell = row.createCell(COLUMN_INDEX_SCHOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TRƯỜNG HỌC");

        cell = row.createCell(COLUMN_INDEX_LEVELJOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("VỊ TRÍ TUYỂN DỤNG");

        cell = row.createCell(COLUMN_INDEX_CV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("CV");

        cell = row.createCell(COLUMN_INDEX_HRREF);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("HR REF");

        cell = row.createCell(COLUMN_INDEX_CVTYPE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("CV TYPE");

        cell = row.createCell(COLUMN_INDEX_STATUSCV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TRẠNG THÁI CV");
    }

    // Write data
    private static void writeBook(ProfileEntity profile, Row row) {

        Cell cell = row.createCell(COLUMN_INDEX_FULLNAME);
        cell.setCellValue(profile.getFullName());

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellValue(profile.getEmail());

        cell = row.createCell(COLUMN_INDEX_PHONENUMBER);
        cell.setCellValue(profile.getPhoneNumber());

        cell = row.createCell(COLUMN_INDEX_JOB);
        cell.setCellValue(profile.getJob());

        cell = row.createCell(COLUMN_INDEX_LASTAPPLY);
        cell.setCellValue(profile.getLastApply());

        cell = row.createCell(COLUMN_INDEX_SOURCECV);
        cell.setCellValue(profile.getSourceCV());

        cell = row.createCell(COLUMN_INDEX_TAGS);
        cell.setCellValue(profile.getTags());

        cell = row.createCell(COLUMN_INDEX_DATEOFBIRTH);
        cell.setCellValue(profile.getDateOfBirth());

        cell = row.createCell(COLUMN_INDEX_GENDER);
        cell.setCellValue(profile.getGender());

        cell = row.createCell(COLUMN_INDEX_HOMETOWN);
        cell.setCellValue(profile.getHometown());

        cell = row.createCell(COLUMN_INDEX_NOTE);
        cell.setCellValue(profile.getNote());

        cell = row.createCell(COLUMN_INDEX_DATEOFCREATE);
        cell.setCellValue(profile.getDateOfCreate());

        cell = row.createCell(COLUMN_INDEX_DATEOFAPPLY);
        cell.setCellValue(profile.getDateOfApply());

        cell = row.createCell(COLUMN_INDEX_DATEOFUPDATE);
        cell.setCellValue(profile.getDateOfUpdate());

        cell = row.createCell(COLUMN_INDEX_EVALUATION);
        cell.setCellValue(profile.getEvaluation());

        cell = row.createCell(COLUMN_INDEX_SCHOOL);
        cell.setCellValue(profile.getSchool());

        cell = row.createCell(COLUMN_INDEX_LEVELJOB);
        cell.setCellValue(profile.getLevelJob());

        cell = row.createCell(COLUMN_INDEX_CV);
        cell.setCellValue(profile.getCv());

        cell = row.createCell(COLUMN_INDEX_HRREF);
        cell.setCellValue(profile.getHrRef());

        cell = row.createCell(COLUMN_INDEX_CVTYPE);
        cell.setCellValue(profile.getCvType());

        cell = row.createCell(COLUMN_INDEX_STATUSCV);
        cell.setCellValue(profile.getStatusCV());
    }

    // Create CellStyle for header
    private static CellStyle createStyleForHeader(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 11); // font size

        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    // Create output file
    private static void createOutputFile(Workbook workbook, String excelFilePath) throws IOException {
        try (OutputStream os = new FileOutputStream(excelFilePath)) {
            workbook.write(os);
        }
    }

}
