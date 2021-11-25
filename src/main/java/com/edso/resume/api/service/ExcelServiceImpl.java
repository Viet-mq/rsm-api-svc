package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileExcelEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.mongodb.client.FindIterable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelServiceImpl extends BaseService implements ExcelService {

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

    public ExcelServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    public static String writeExcel(List<ProfileExcelEntity> profiles, String excelFilePath) throws IOException {
        // Create Workbook
        Workbook workbook = getWorkbook(excelFilePath);

        // Create sheet
        Sheet sheet = workbook.createSheet("Profiles"); // Create sheet with sheet name

        int rowIndex = 0;

        // Write header
        writeHeader(sheet, rowIndex);

        // Write data
        rowIndex++;
        for (ProfileExcelEntity profile : profiles) {
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
    private static void writeProfile(ProfileExcelEntity profile, Sheet sheet, Row row) {

        CellStyle cellStyle = createStyleForRow(sheet);

        Cell cell = row.createCell(COLUMN_INDEX_FULLNAME);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getFullName());

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getEmail());

        cell = row.createCell(COLUMN_INDEX_PHONENUMBER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getPhoneNumber());

        cell = row.createCell(COLUMN_INDEX_JOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getJobName());

        cell = row.createCell(COLUMN_INDEX_LASTAPPLY);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getLastApply());

        cell = row.createCell(COLUMN_INDEX_SOURCECV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getSourceCVName());

        cell = row.createCell(COLUMN_INDEX_TAGS);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getTags());

        cell = row.createCell(COLUMN_INDEX_DATEOFBIRTH);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getDateOfBirth());

        cell = row.createCell(COLUMN_INDEX_GENDER);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getGender());

        cell = row.createCell(COLUMN_INDEX_HOMETOWN);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getHometown());

        cell = row.createCell(COLUMN_INDEX_NOTE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getNote());

        cell = row.createCell(COLUMN_INDEX_DATEOFCREATE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getDateOfCreate());

        cell = row.createCell(COLUMN_INDEX_DATEOFAPPLY);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getDateOfApply());

        cell = row.createCell(COLUMN_INDEX_DATEOFUPDATE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getDateOfUpdate());

        cell = row.createCell(COLUMN_INDEX_EVALUATION);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getEvaluation());

        cell = row.createCell(COLUMN_INDEX_SCHOOL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getSchoolName());

        cell = row.createCell(COLUMN_INDEX_LEVELJOB);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getLevelJobName());

        cell = row.createCell(COLUMN_INDEX_CV);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getCv());

        cell = row.createCell(COLUMN_INDEX_HRREF);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getHrRef());

        cell = row.createCell(COLUMN_INDEX_CVTYPE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(profile.getCvType());

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
    public String exportExcel(HeaderInfo info) {
        try {
            FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_PROFILE, null, null, 0, 0);
            List<ProfileExcelEntity> profiles = new ArrayList<>();
            if (lst != null) {
                for (Document doc : lst) {
                    ProfileExcelEntity profile = ProfileExcelEntity.builder()
                            .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                            .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                            .dateOfBirth(parseDateMonthYear(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_BIRTH))))
                            .hometown(AppUtils.parseString(doc.get(DbKeyConfig.HOMETOWN)))
                            .schoolName(AppUtils.parseString(doc.get(DbKeyConfig.SCHOOL_NAME)))
                            .phoneNumber(AppUtils.parseString(doc.get(DbKeyConfig.PHONE_NUMBER)))
                            .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                            .jobName(AppUtils.parseString(doc.get(DbKeyConfig.JOB_NAME)))
                            .levelJobName(AppUtils.parseString(doc.get(DbKeyConfig.LEVEL_JOB_NAME)))
                            .cv(AppUtils.parseString(doc.get(DbKeyConfig.CV)))
                            .sourceCVName(AppUtils.parseString(doc.get(DbKeyConfig.SOURCE_CV_NAME)))
                            .hrRef(AppUtils.parseString(doc.get(DbKeyConfig.HR_REF)))
                            .dateOfApply(parseDate(AppUtils.parseLong(doc.get(DbKeyConfig.DATE_OF_APPLY))))
                            .cvType(AppUtils.parseString(doc.get(DbKeyConfig.CV_TYPE)))
                            .statusCVName(AppUtils.parseString(doc.get(DbKeyConfig.STATUS_CV_NAME)))
                            .lastApply(parseDate(AppUtils.parseLong(doc.get(DbKeyConfig.LAST_APPLY))))
                            .tags(AppUtils.parseString(doc.get(DbKeyConfig.TAGS)))
                            .gender(AppUtils.parseString(doc.get(DbKeyConfig.GENDER)))
                            .note(AppUtils.parseString(doc.get(DbKeyConfig.NOTE)))
                            .dateOfCreate(parseDate(AppUtils.parseLong(doc.get(DbKeyConfig.CREATE_AT))))
                            .dateOfUpdate(parseDate(AppUtils.parseLong(doc.get(DbKeyConfig.UPDATE_AT))))
                            .evaluation(AppUtils.parseString(doc.get(DbKeyConfig.EVALUATION)))
                            .build();
                    profiles.add(profile);
                }
            }
            return writeExcel(profiles, path);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
            return null;
        }
    }
}
