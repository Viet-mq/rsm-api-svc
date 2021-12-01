package com.edso.resume.api.exporter;

import com.edso.resume.api.domain.entities.ReportRecruitmentResultEntity;
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

@Component
public class ReportRecruitmentResultExporter extends BaseExporter {
    public ExportResponse exportReportRecruitmentActivities(List<ReportRecruitmentResultEntity> report, String excelFilePath, Long from, Long to) {
        ExportResponse response = new ExportResponse();
        try {
            // Create Workbook
            Workbook workbook = getWorkbook(excelFilePath);

            // Create sheet
            Sheet sheet = workbook.createSheet("Report"); // Create sheet with sheet name

            writeBanner(from, to, sheet);

            // Write header
            writeHeader(sheet);

            // Write data
            int rowIndex = 5;
            for (ReportRecruitmentResultEntity reportRecruitmentResultEntity : report) {
                // Create row
                Row row = sheet.createRow(rowIndex);
                // Write data on row
                writeReport(reportRecruitmentResultEntity, sheet, row, rowIndex - 4);
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
    private static void writeHeader(Sheet sheet) {
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
        cell.setCellValue("Vị trí tuyển dụng");

        cell = row.createCell(2);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Cần tuyển");

        cell = row.createCell(3);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Đã tuyển");

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Tỷ lệ hoàn thành");

    }


    // Write data
    private static void writeReport(ReportRecruitmentResultEntity report, Sheet sheet, Row row, int stt) {
        CellStyle cellStyle = createStyleForRow(sheet);

        CellStyle cellStyleSTT = createStyleForRow(sheet);
        cellStyleSTT.setAlignment(HorizontalAlignment.CENTER);

        CellStyle cellStylePercent = createStyleForRow(sheet);
        cellStylePercent.setAlignment(HorizontalAlignment.RIGHT);

        Cell cell = row.createCell(0);
        cell.setCellStyle(cellStyleSTT);
        cell.setCellValue(stt);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getRecruitmentName());

        cell = row.createCell(2);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getNeedToRecruit());

        cell = row.createCell(3);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getRecruited());

        cell = row.createCell(4);
        cell.setCellStyle(cellStylePercent);
        cell.setCellValue(report.getPercent());

    }

    private static void writeBanner(Long from, Long to, Sheet sheet) {
        CellStyle cellStyle = createStyleForBanner(sheet);
        CellStyle cellStyle2 = createStyleForBanner2(sheet);

        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);

        Cell cell = row1.createCell(0);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("THỐNG KÊ KẾT QUẢ TUYỂN DỤNG THEO TIN TUYỂN DỤNG");

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

    private static CellStyle createStyleForBanner(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 16); // font size

        CellRangeAddress ca = new CellRangeAddress(1, 1, 0, 4);
        sheet.addMergedRegion(ca);

        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static CellStyle createStyleForBanner2(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12); // font size

        CellRangeAddress ca = new CellRangeAddress(2, 2, 0, 4);
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
