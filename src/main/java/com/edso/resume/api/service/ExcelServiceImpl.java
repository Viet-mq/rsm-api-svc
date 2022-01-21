package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.entities.SkillEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExcelServiceImpl extends BaseService implements ExcelService {

    private static final int COLUMN_INDEX_FULLNAME = 0;
    private static final int COLUMN_INDEX_GENDER = 1;
    private static final int COLUMN_INDEX_PHONENUMBER = 2;
    private static final int COLUMN_INDEX_EMAIL = 3;
    private static final int COLUMN_INDEX_DATEOFBIRTH = 4;
    private static final int COLUMN_INDEX_HOMETOWN = 5;
    private static final int COLUMN_INDEX_LEVELSCHOOL = 6;
    private static final int COLUMN_INDEX_SCHOOL = 7;
    private static final int COLUMN_INDEX_DATEOFAPPLY = 8;
    private static final int COLUMN_INDEX_SOURCECV = 9;
    private static final int COLUMN_INDEX_JOB = 10;
    private static final int COLUMN_INDEX_SKILL = 11;
    private static final int COLUMN_INDEX_LEVELJOB = 12;
    private static final int COLUMN_INDEX_RECRUITMENT = 13;
    private static final int COLUMN_INDEX_TALENTPOOL = 14;
    private static final int COLUMN_INDEX_HRREF = 15;
    private static final int COLUMN_INDEX_MAILREF = 16;
    private static final int COLUMN_INDEX_DEPARTMENT = 17;
    private static final int COLUMN_INDEX_STATUSCV = 18;

    public ExcelServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    public static String writeExcel(List<ProfileEntity> profiles, String excelFilePath) throws IOException {
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
            writeProfile(profile, sheet, row);
            rowIndex++;
        }

        // Create file excel
        createOutputFile(workbook, excelFilePath);

        return excelFilePath;
    }

//    private static byte[] openFile(String excelFilePath) throws IOException {
//        RandomAccessFile f = new RandomAccessFile(excelFilePath, "r");
//        byte[] b = new byte[(int) f.length()];
//        f.readFully(b);
//        f.close();
//        return b;
//    }

    // Create workbook
    private static Workbook getWorkbook(String excelFilePath) {
        Workbook workbook;
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
        cell.setCellValue("Họ và tên");

        cell = row.createCell(COLUMN_INDEX_GENDER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Giới tính");

        cell = row.createCell(COLUMN_INDEX_PHONENUMBER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Số điện thoại");

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Email");

        cell = row.createCell(COLUMN_INDEX_DATEOFBIRTH);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Ngày sinh");

        cell = row.createCell(COLUMN_INDEX_HOMETOWN);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Quê quán");

        cell = row.createCell(COLUMN_INDEX_LEVELSCHOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Trình độ học vấn");

        cell = row.createCell(COLUMN_INDEX_SCHOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Trường học");

        cell = row.createCell(COLUMN_INDEX_DATEOFAPPLY);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Ngày ứng tuyển");

        cell = row.createCell(COLUMN_INDEX_SOURCECV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Nguồn ứng viên");

        cell = row.createCell(COLUMN_INDEX_JOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Công việc");

        cell = row.createCell(COLUMN_INDEX_SKILL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Kỹ năng");

        cell = row.createCell(COLUMN_INDEX_LEVELJOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Cấp bậc");

        cell = row.createCell(COLUMN_INDEX_RECRUITMENT);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Tin tuyển dụng");

        cell = row.createCell(COLUMN_INDEX_TALENTPOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Kho tiềm năng");

        cell = row.createCell(COLUMN_INDEX_HRREF);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Người giới thiệu");

        cell = row.createCell(COLUMN_INDEX_MAILREF);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Email người giới thiệu");

        cell = row.createCell(COLUMN_INDEX_DEPARTMENT);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Phòng ban");

        cell = row.createCell(COLUMN_INDEX_STATUSCV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Vòng tuyển dụng");

    }

    // Write data
    private static void writeProfile(ProfileEntity profile, Sheet sheet, Row row) {

        CellStyle cellStyle = createStyleForRow(sheet);

        Cell cell = row.createCell(COLUMN_INDEX_FULLNAME);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getFullName());

        cell = row.createCell(COLUMN_INDEX_GENDER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getGender());

        cell = row.createCell(COLUMN_INDEX_PHONENUMBER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getPhoneNumber());

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getEmail());

        cell = row.createCell(COLUMN_INDEX_DATEOFBIRTH);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(AppUtils.formatDate(new Date(profile.getDateOfBirth()), "ddMMyyyy"));

        cell = row.createCell(COLUMN_INDEX_HOMETOWN);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getHometown());

        cell = row.createCell(COLUMN_INDEX_LEVELSCHOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getLevelSchool());

        cell = row.createCell(COLUMN_INDEX_SCHOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getSchoolName());

        cell = row.createCell(COLUMN_INDEX_DATEOFAPPLY);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(AppUtils.formatDate(new Date(profile.getDateOfApply()), "ddMMyyyy"));

        cell = row.createCell(COLUMN_INDEX_SOURCECV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getSourceCVName());

        cell = row.createCell(COLUMN_INDEX_JOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getJobName());


        cell = row.createCell(COLUMN_INDEX_SKILL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getSkill().stream().map(SkillEntity::getName).collect(Collectors.joining(", ")));

        cell = row.createCell(COLUMN_INDEX_LEVELJOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getLevelJobName());

        cell = row.createCell(COLUMN_INDEX_RECRUITMENT);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getRecruitmentName());

        cell = row.createCell(COLUMN_INDEX_TALENTPOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getTalentPoolName());

        cell = row.createCell(COLUMN_INDEX_HRREF);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getHrRef());

        cell = row.createCell(COLUMN_INDEX_MAILREF);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getMailRef());

        cell = row.createCell(COLUMN_INDEX_DEPARTMENT);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getDepartmentName());

        cell = row.createCell(COLUMN_INDEX_STATUSCV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getStatusCVName());
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

    private static CellStyle createStyleForRow(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
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

    @Value("${excel.path}")
    private String path;

    @Override
    public String exportExcel(HeaderInfo info, String fullName, String talentPool, String job, String levelJob, String department, String recruitment, String calendar, String statusCV) {
        try {
            List<Bson> c = new ArrayList<>();
            if (!Strings.isNullOrEmpty(fullName)) {
                c.add(Filters.regex(DbKeyConfig.NAME_SEARCH, Pattern.compile(AppUtils.parseVietnameseToEnglish(fullName))));
            }
            if (!Strings.isNullOrEmpty(talentPool)) {
                c.add(Filters.eq(DbKeyConfig.TALENT_POOL_ID, talentPool));
            }
            if (!Strings.isNullOrEmpty(job)) {
                c.add(Filters.eq(DbKeyConfig.JOB_ID, job));
            }
            if (!Strings.isNullOrEmpty(levelJob)) {
                c.add(Filters.eq(DbKeyConfig.LEVEL_JOB_ID, levelJob));
            }
            if (!Strings.isNullOrEmpty(department)) {
                c.add(Filters.eq(DbKeyConfig.DEPARTMENT_ID, department));
            }
            if (!Strings.isNullOrEmpty(recruitment)) {
                c.add(Filters.eq(DbKeyConfig.RECRUITMENT_ID, recruitment));
            }
            if (!Strings.isNullOrEmpty(statusCV)) {
                c.add(Filters.eq(DbKeyConfig.STATUS_CV_ID, statusCV));
            }
            if (!Strings.isNullOrEmpty(calendar)) {
                if (calendar.equals("set")) {
                    c.add(Filters.eq(DbKeyConfig.CALENDAR, -1));
                }
                if (calendar.equals("notset")) {
                    c.add(Filters.ne(DbKeyConfig.CALENDAR, 1));
                }
            }
            Bson cond = buildCondition(c);
            Bson sort = Filters.eq(DbKeyConfig.CREATE_AT, -1);
            FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, cond, sort, 0, 0);
            List<ProfileEntity> rows = new ArrayList<>();
            if (lst != null) {
                for (Document doc : lst) {
                    ProfileEntity profile = ProfileEntity.builder()
                            .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                            .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                            .gender(AppUtils.parseString(doc.get(DbKeyConfig.GENDER)))
                            .dateOfBirth(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_BIRTH)))
                            .hometown(AppUtils.parseString(doc.get(DbKeyConfig.HOMETOWN)))
                            .schoolId(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_ID)))
                            .schoolName(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_NAME)))
                            .phoneNumber(AppUtils.parseString(doc.get(DbKeyConfig.PHONE_NUMBER)))
                            .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                            .jobId(AppUtils.parseString(doc.get(DbKeyConfig.JOB_ID)))
                            .jobName(AppUtils.parseString(doc.get(DbKeyConfig.JOB_NAME)))
                            .levelJobId(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_ID)))
                            .levelJobName(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_NAME)))
                            .sourceCVId(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_ID)))
                            .sourceCVName(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_NAME)))
                            .hrRef(AppUtils.parseString(doc.get(DbKeyConfig.HR_REF)))
                            .dateOfApply(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_APPLY)))
                            .statusCVId(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_ID)))
                            .statusCVName(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)))
                            .talentPoolId(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_ID)))
                            .talentPoolName(AppUtils.parseString(doc.get(DbKeyConfig.TALENT_POOL_NAME)))
                            .image(AppUtils.parseString(doc.get(DbKeyConfig.URL_IMAGE)))
                            .cv(AppUtils.parseString(doc.get(DbKeyConfig.CV)))
                            .urlCV(AppUtils.parseString(doc.get(DbKeyConfig.URL_CV)))
                            .departmentId(AppUtils.parseString(doc.get(DbKeyConfig.DEPARTMENT_ID)))
                            .departmentName(AppUtils.parseString(doc.get(DbKeyConfig.DEPARTMENT_NAME)))
                            .levelSchool(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_SCHOOL)))
                            .recruitmentId(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_ID)))
                            .recruitmentName(AppUtils.parseString(doc.get(DbKeyConfig.RECRUITMENT_NAME)))
                            .mailRef(AppUtils.parseString(doc.get(DbKeyConfig.MAIL_REF)))
                            .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                            .skill((List<SkillEntity>) doc.get(DbKeyConfig.SKILL))
                            .avatarColor(AppUtils.parseString(doc.get(DbKeyConfig.AVATAR_COLOR)))
                            .isNew(AppUtils.parseString(doc.get(DbKeyConfig.IS_NEW)))
                            .build();
                    rows.add(profile);
                }
            }
            return writeExcel(rows, path);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            return null;
        }
    }
}
