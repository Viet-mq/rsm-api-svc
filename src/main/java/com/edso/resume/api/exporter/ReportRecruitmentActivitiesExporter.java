package com.edso.resume.api.exporter;

import com.edso.resume.api.domain.entities.ReportRecruitmentActivitiesEntity;
import com.edso.resume.api.domain.entities.StatusEntity;
import com.edso.resume.api.domain.response.ExportResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class ReportRecruitmentActivitiesExporter extends BaseExporter {
    public ExportResponse exportReportRecruitmentActivities(List<ReportRecruitmentActivitiesEntity> report, Set<String> headers, String excelFilePath, Long from, Long to) {
        ExportResponse response = new ExportResponse();
        try {
            // Create Workbook
            Workbook workbook = getWorkbook(excelFilePath);

            // Create sheet
            Sheet sheet = workbook.createSheet("Report"); // Create sheet with sheet name

            writeBanner(from, to, sheet, headers.size() + 4);

            // Write header
            writeHeader(headers, sheet);

            // Write data
            int rowIndex = 5;
            for (ReportRecruitmentActivitiesEntity reportRecruitmentActivitiesEntity : report) {
                // Create row
                Row row = sheet.createRow(rowIndex);
                // Write data on row
                writeReport(reportRecruitmentActivitiesEntity, sheet, row, rowIndex - 4);
                rowIndex++;
            }

            // Create file excel
            createOutputFile(workbook, excelFilePath);

            response.setPath(excelFilePath);
            return response;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            return null;
        }
    }

    // Create workbook
    private static Workbook getWorkbook(String excelFilePath) {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook();
        }
        return workbook;
    }

    // Write header with format
    private static void writeHeader(Set<String> headers, Sheet sheet) {
        // create CellStyle
        CellStyle cellStyle = createStyleForHeader(sheet);

        // Create row
        Row row = sheet.createRow(4);

        // Create cells
        Cell cell = row.createCell(0);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("STT");

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Thành viên");

        cell = row.createCell(2);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Username");

        cell = row.createCell(3);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Tin đã tạo");

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Đánh giá");

        int i = 5;
        for (String header : headers) {
            cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(header);
            i++;
        }
    }


    // Write data
    private static void writeReport(ReportRecruitmentActivitiesEntity report, Sheet sheet, Row row, int stt) {
        CellStyle cellStyle = createStyleForRow(sheet);

        CellStyle cellStyleSTT = createStyleForRow(sheet);
        cellStyleSTT.setAlignment(HorizontalAlignment.CENTER);

        Cell cell = row.createCell(0);
        cell.setCellStyle(cellStyleSTT);
        cell.setCellValue(stt);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getFullName());

        cell = row.createCell(2);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getCreateBy());

        cell = row.createCell(3);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getRecruitmentTotal());

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getNoteTotal());

        int i = 5;
        List<StatusEntity> list = report.getStatus();
        for (StatusEntity statusEntity : list) {
            cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statusEntity.getCount());
            i++;
        }

    }

    private static void writeBanner(Long from, Long to, Sheet sheet, int cellIndex) {
        CellStyle cellStyle = createStyleForBanner(sheet, cellIndex);
        CellStyle cellStyle2 = createStyleForBanner2(sheet, cellIndex);

        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);

        Cell cell = row1.createCell(0);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TỔNG HỢP HOẠT ĐỘNG TUYỂN DỤNG");

        cell = row2.createCell(0);
        cell.setCellStyle(cellStyle2);
        cell.setCellValue("Từ " + parseDate(from) + " đến " + parseDate(to));

    }

    public static String parseDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
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

    private static CellStyle createStyleForBanner(Sheet sheet, int cell) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 16); // font size

        CellRangeAddress ca = new CellRangeAddress(1, 1, 0, cell);
        sheet.addMergedRegion(ca);

        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static CellStyle createStyleForBanner2(Sheet sheet, int cell) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12); // font size

        CellRangeAddress ca = new CellRangeAddress(2, 2, 0, cell);
        sheet.addMergedRegion(ca);

        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
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
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
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
