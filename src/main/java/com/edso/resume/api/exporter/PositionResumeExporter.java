package com.edso.resume.api.exporter;

import com.edso.resume.api.domain.entities.ReportByDepartmentEntity;
import com.edso.resume.api.domain.entities.SourceEntity;
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
public class PositionResumeExporter extends BaseExporter {

    public ExportResponse exportReportByDepartment(List<ReportByDepartmentEntity> report, Set<String> headers, String excelFilePath, Long from, Long to) {
        ExportResponse response = new ExportResponse();
        try {
            // Create Workbook
            Workbook workbook = getWorkbook(excelFilePath);

            // Create sheet
            Sheet sheet = workbook.createSheet("Report"); // Create sheet with sheet name

            writeBanner(from, to, sheet, headers.size() + 2);

            // Write header
            writeHeader(headers, sheet);

            // Write data
            int rowIndex = 6;
            for (ReportByDepartmentEntity reportByDepartmentEntity : report) {
                // Create row
                Row row = sheet.createRow(rowIndex);
                // Write data on row
                writeReport(reportByDepartmentEntity, sheet, row, rowIndex - 5);
                rowIndex++;
            }

            writeFooter(sheet, report.size() + 5, headers.size() + 2);

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

        CellRangeAddress ca1 = new CellRangeAddress(4, 5, 0, 0);
        sheet.addMergedRegion(ca1);
        CellRangeAddress ca2 = new CellRangeAddress(4, 5, 1, 1);
        sheet.addMergedRegion(ca2);

        // Create row
        Row row1 = sheet.createRow(4);
        Row row2 = sheet.createRow(5);

        // Create cells
        Cell cell = row1.createCell(0);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("STT");

        cell = row2.createCell(0);
        cell.setCellStyle(cellStyle);

        cell = row1.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Tin tuyển dụng");

        cell = row2.createCell(1);
        cell.setCellStyle(cellStyle);

        int i = 2;
        for (String header : headers) {
            cell = row1.createCell(i);
            cell.setCellStyle(cellStyle);
            cell = row2.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(header);
            i++;
        }

        cell = row1.createCell(i);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Tổng");

        cell = row2.createCell(i);
        cell.setCellStyle(cellStyle);

        CellRangeAddress ca3 = new CellRangeAddress(4, 5, i, i);
        sheet.addMergedRegion(ca3);

        cell = row1.getCell(2);
        cell.setCellValue("Nguồn ứng viên");

        i--;
        CellRangeAddress ca4 = new CellRangeAddress(4, 4, 2, i);
        sheet.addMergedRegion(ca4);
    }


    // Write data
    private static void writeReport(ReportByDepartmentEntity report, Sheet sheet, Row row, int stt) {
        CellStyle cellStyle = createStyleForRow(sheet);

        CellStyle cellStyleSTT = createStyleForRow(sheet);
        cellStyleSTT.setAlignment(HorizontalAlignment.CENTER);

        Cell cell = row.createCell(0);
        cell.setCellStyle(cellStyleSTT);
        cell.setCellValue(stt);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(report.getRecruitmentName());

        int i = 2;
        int total = 0;
        List<SourceEntity> list = report.getSources();
        for (SourceEntity sourceEntity : list) {
            cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(sourceEntity.getCount());
            total += sourceEntity.getCount();
            i++;
        }

        cell = row.createCell(i);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(total);
    }

    private static void writeBanner(Long from, Long to, Sheet sheet, int cellIndex) {
        CellStyle cellStyle = createStyleForBanner(sheet, cellIndex);
        CellStyle cellStyle2 = createStyleForBanner2(sheet, cellIndex);

        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);

        Cell cell = row1.createCell(0);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("BÁO CÁO TỔNG HỢP NGUỒN ỨNG VIÊN THEO VỊ TRÍ TUYỂN DỤNG");

        cell = row2.createCell(0);
        cell.setCellStyle(cellStyle2);
        cell.setCellValue("Từ " + parseDate(from) + " đến " + parseDate(to));

    }

    private static void writeFooter(Sheet sheet, int rowCount, int cellCount) {

        CellStyle cellStyle = createStyleForFooter(sheet);

        Row row = sheet.createRow(rowCount + 1);

        Cell cell = row.createCell(0);
        cell.setCellStyle(cellStyle);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Tổng");

        for (int j = 2; j <= cellCount; j++) {
            int sum = 0;
            for (int i = 6; i <= rowCount; i++) {
                Row row2 = sheet.getRow(i);
                sum += row2.getCell(j).getNumericCellValue();
            }
            cell = row.createCell(j);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(sum);
        }

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

    private static CellStyle createStyleForFooter(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);
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
