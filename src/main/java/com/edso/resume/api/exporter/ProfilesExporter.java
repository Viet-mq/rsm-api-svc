package com.edso.resume.api.exporter;

import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.entities.SkillEntity;
import com.edso.resume.lib.common.AppUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfilesExporter {
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

    public static String writeExcel(List<ProfileEntity> profiles, String excelFilePath) throws IOException {
        // Create Workbook
        Workbook workbook = getWorkbook(excelFilePath);

        // Create sheet
        Sheet sheet = workbook.createSheet("Profiles"); // Create sheet with sheet name
        sheet.setColumnWidth(0, 7000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 7000);
        sheet.setColumnWidth(4, 3000);
        sheet.setColumnWidth(5, 10000);
        sheet.setColumnWidth(6, 5000);
        sheet.setColumnWidth(7, 10000);
        sheet.setColumnWidth(8, 4000);
        sheet.setColumnWidth(9, 4000);
        sheet.setColumnWidth(10, 7000);
        sheet.setColumnWidth(11, 7000);
        sheet.setColumnWidth(12, 7000);
        sheet.setColumnWidth(13, 7000);
        sheet.setColumnWidth(14, 7000);
        sheet.setColumnWidth(15, 7000);
        sheet.setColumnWidth(16, 7000);
        sheet.setColumnWidth(17, 5000);
        sheet.setColumnWidth(18, 5000);

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
        cell.setCellValue(AppUtils.formatDateToString(new Date(profile.getDateOfBirth()), "dd/MM/yyyy"));

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
        cell.setCellValue(AppUtils.formatDateToString(new Date(profile.getDateOfApply()), "dd/MM/yyyy"));

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
        font.setFontHeightInPoints((short) 12); // font size

        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static CellStyle createStyleForRow(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12); // font size
        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
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
