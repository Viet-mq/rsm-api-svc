package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.BlacklistEntity;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.entities.HeaderInfo;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class BlacklistExcelServiceImpl extends BaseService implements BlacklistExcelService {

    private static final int COLUMN_INDEX_NAME = 0;
    private static final int COLUMN_INDEX_EMAIL = 1;
    private static final int COLUMN_INDEX_PHONE = 2;
    private static final int COLUMN_INDEX_SSN = 3;
    private static final int COLUMN_INDEX_REASON = 4;

    public BlacklistExcelServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
    }

    @Override
    public byte[] export(HeaderInfo headerInfo, String name) throws IOException {
        final String blacklistFilePath = "D:\\Blacklists.xlsx";
        List<Bson> c = new ArrayList<>();
        if (!Strings.isNullOrEmpty(name)) {
            c.add(Filters.regex("name_search", Pattern.compile(name.toLowerCase())));
        }
        Bson cond = buildCondition(c);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_BLACKLIST, cond, null, 0, 0);
        List<BlacklistEntity> blacklists = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                BlacklistEntity blacklist = BlacklistEntity.builder()
                        .id(AppUtils.parseString(doc.get("id")))
                        .name(AppUtils.parseString(doc.get("name")))
                        .email(AppUtils.parseString(doc.get("email")))
                        .phoneNumber(AppUtils.parseString(doc.get("phoneNumber")))
                        .ssn(AppUtils.parseString(doc.get("ssn")))
                        .reason(AppUtils.parseString(doc.get("reason")))
                        .build();
                blacklists.add(blacklist);
            }
        }
        return writeExcel(blacklists, blacklistFilePath);
    }

    public static byte[] writeExcel(List<BlacklistEntity> blacklists, String excelFilePath) throws IOException {

        Workbook workbook = getWorkbook(excelFilePath);
        Sheet sheet = workbook.createSheet("Blacklists");
        int rowIndex = 0;
        writeHeader(sheet, rowIndex);

        // Write data
        rowIndex++;
        for (BlacklistEntity blacklist : blacklists) {
            Row row = sheet.createRow(rowIndex);
            writeBook(blacklist, row);
            rowIndex++;
        }

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

    private static void writeHeader(Sheet sheet, int rowIndex) {

        CellStyle cellStyle = createStyleForHeader(sheet);

        Row row = sheet.createRow(rowIndex);

        Cell cell = row.createCell(COLUMN_INDEX_NAME);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("TÊN BLACKLIST");

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("EMAIL");

        cell = row.createCell(COLUMN_INDEX_PHONE);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("SỐ ĐIỆN THOẠI");

        cell = row.createCell(COLUMN_INDEX_SSN);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("SỐ CCCD");

        cell = row.createCell(COLUMN_INDEX_REASON);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("LÝ DO");

    }

    private static void writeBook(BlacklistEntity blacklist, Row row) {

        Cell cell = row.createCell(COLUMN_INDEX_NAME);
        cell.setCellValue(blacklist.getName());

        cell = row.createCell(COLUMN_INDEX_EMAIL);
        cell.setCellValue(blacklist.getEmail());

        cell = row.createCell(COLUMN_INDEX_PHONE);
        cell.setCellValue(blacklist.getPhoneNumber());

        cell = row.createCell(COLUMN_INDEX_SSN);
        cell.setCellValue(blacklist.getSsn());

        cell = row.createCell(COLUMN_INDEX_REASON);
        cell.setCellValue(blacklist.getReason());

    }

    private static CellStyle createStyleForHeader(Sheet sheet) {
        // Create font
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);

        // Create CellStyle
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static void createOutputFile(Workbook workbook, String excelFilePath) throws IOException {
        try (OutputStream os = new FileOutputStream(excelFilePath)) {
            workbook.write(os);
        }
    }
}
