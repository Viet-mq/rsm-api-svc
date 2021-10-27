package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.ProfileUploadEntity;
import com.edso.resume.api.domain.validator.DictionaryNameValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryNameValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryNameValidator;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.ThreadConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UploadProfilesServiceImpl extends BaseService implements UploadProfilesService, IDictionaryNameValidator {

    public static final int COLUMN_FULL_NAME = 0;
    public static final int COLUMN_PHONE_NUMBER = 1;
    public static final int COLUMN_EMAIL = 2;
    public static final int COLUMN_DATE_OF_BIRTH = 3;
    public static final int COLUMN_GENDER = 4;
    public static final int COLUMN_HOMETOWN = 5;
    public static final int COLUMN_SCHOOL_LEVEL = 6;
    public static final int COLUMN_SCHOOL_NAME = 7;
    public static final int COLUMN_MAJOR = 8;
    public static final int COLUMN_RECENT_WORK_PLACE = 9;
    public static final int COLUMN_DATE_OF_APPLY = 10;
    public static final int COLUMN_SOURCE_CV = 11;

    private final Queue<DictionaryNameValidatorResult> queue = new LinkedBlockingQueue<>();
    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
    private static Pattern patternEmail;
    @JsonIgnore
    protected HeaderInfo info;

    @Value("${excel.serverPath}")
    private String serverPath;

    public UploadProfilesServiceImpl(MongoDbOnlineSyncActions db) {
        super(db);
        patternEmail = Pattern.compile(EMAIL_REGEX);
    }

    // Get cell value
    private static Object getCellValue(Cell cell) {
        CellType cellType = cell.getCellType();
        Object cellValue = null;
        switch (cellType) {
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case FORMULA:
                Workbook workbook = cell.getSheet().getWorkbook();
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                cellValue = evaluator.evaluate(cell).getNumberValue();
                break;
            case NUMERIC:
                cellValue = cell.getNumericCellValue();
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            default:
                break;
        }

        return cellValue;
    }

    public List<ProfileUploadEntity> readExcel(MultipartFile excelFile) throws IOException {
        List<ProfileUploadEntity> listProfile = new ArrayList<>();

        File file = convertToFile(excelFile);
        if (file == null) {
            return null;
        }

        // Get file
        InputStream inputStream = new FileInputStream(file);

        // Get workbook
        Workbook workbook = getWorkbook(inputStream, file.getPath());

        // Get sheet
        Sheet sheet = workbook.getSheetAt(0);

        // Get all rows
        for (Row nextRow : sheet) {
            if (nextRow.getRowNum() < 4) {
                // Ignore header
                continue;
            }

            // Get all cells
            Iterator<Cell> cellIterator = nextRow.cellIterator();

            // Read cells and set value for book object
            ProfileUploadEntity profiles = new ProfileUploadEntity();
            while (cellIterator.hasNext()) {
                //Read cell
                Cell cell = cellIterator.next();
                Object cellValue = getCellValue(cell);
                if (cellValue == null || cellValue.toString().isEmpty()) {
                    continue;
                }
                // Set value for book object
                int columnIndex = cell.getColumnIndex();
                switch (columnIndex) {
                    case COLUMN_FULL_NAME:
                        profiles.setFullName((String) getCellValue(cell));
                        break;
                    case COLUMN_PHONE_NUMBER:
                        profiles.setPhoneNumber((String) getCellValue(cell));
                        break;
                    case COLUMN_EMAIL:
                        profiles.setEmail((String) getCellValue(cell));
                        break;
                    case COLUMN_DATE_OF_BIRTH:
                        profiles.setDateOfBirth((String) getCellValue(cell));
                        break;
                    case COLUMN_GENDER:
                        profiles.setGender((String) getCellValue(cell));
                        break;
                    case COLUMN_HOMETOWN:
                        profiles.setHometown((String) getCellValue(cell));
                        break;
                    case COLUMN_SCHOOL_LEVEL:
                        profiles.setSchoolLevel((String) getCellValue(cell));
                        break;
                    case COLUMN_SCHOOL_NAME:
                        profiles.setSchoolName((String) getCellValue(cell));
                        break;
                    case COLUMN_MAJOR:
                        profiles.setMajor((String) getCellValue(cell));
                        break;
                    case COLUMN_RECENT_WORK_PLACE:
                        profiles.setRecentWorkPlace((String) getCellValue(cell));
                        break;
                    case COLUMN_DATE_OF_APPLY:
                        profiles.setDateOfApply((String) getCellValue(cell));
                        break;
                    case COLUMN_SOURCE_CV:
                        profiles.setSourceCVName((String) getCellValue(cell));
                        break;
                    default:
                        break;
                }

            }
            listProfile.add(profiles);
        }
        workbook.close();

        return listProfile;
    }

    // Get Workbook
    private Workbook getWorkbook(InputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        }
        return workbook;
    }

    @Override
    public BaseResponse uploadProfiles(MultipartFile request, HeaderInfo info) {
        BaseResponse response = new BaseResponse();
        List<ProfileUploadEntity> profiles = null;
        try {
            profiles = readExcel(request);
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }

        if (profiles == null || profiles.isEmpty()) {
            response.setFailed("Vui lòng nhập đúng file excel!");
            return response;
        }
        for (ProfileUploadEntity profile : profiles) {

            if (Strings.isNullOrEmpty(profile.getFullName())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profile.getEmail())) {
                continue;
            }
            if (!validateEmail(profile.getEmail())) {
                continue;
            }

            String key = UUID.randomUUID().toString();

            try {
                List<DictionaryNameValidateProcessor> rs = new ArrayList<>();
                rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.SCHOOL, profile.getSchoolName(), db, this));
                rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.SOURCE_CV, profile.getSourceCVName(), db, this));
                int total = rs.size();

                for (DictionaryNameValidateProcessor r : rs) {
                    Thread t = new Thread(r);
                    t.start();
                }

                long time = System.currentTimeMillis();
                int count = 0;
                int count2 = 0;
                while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                    DictionaryNameValidatorResult validatorResult = queue.poll();
                    if (validatorResult != null) {
                        if (validatorResult.getKey().equals(key)) {
                            if (!validatorResult.isResult()) {
                                count2++;
                                break;
                            } else {
                                count++;
                            }
                            total--;
                        } else {
                            queue.offer(validatorResult);
                        }
                    }
                }
                if (count2 != 0) {
                    continue;
                }

                if (count != rs.size()) {
                    continue;
                }

                String schoolId = null;
                String sourceCVId = null;

                for (DictionaryNameValidateProcessor r : rs) {
                    switch (r.getResult().getType()) {
                        case ThreadConfig.SCHOOL: {
                            schoolId = r.getResult().getId();
                            break;
                        }
                        case ThreadConfig.SOURCE_CV: {
                            sourceCVId = r.getResult().getId();
                            break;
                        }
                    }
                }

                Document pro = new Document();
                pro.append(DbKeyConfig.ID, UUID.randomUUID().toString());
                pro.append(DbKeyConfig.FULL_NAME, profile.getFullName());
                pro.append(DbKeyConfig.PHONE_NUMBER, profile.getPhoneNumber());
                pro.append(DbKeyConfig.EMAIL, profile.getEmail());
                pro.append(DbKeyConfig.DATE_OF_BIRTH, profile.getDateOfBirth());
                pro.append(DbKeyConfig.GENDER, profile.getGender());
                pro.append(DbKeyConfig.HOMETOWN, profile.getHometown());
                pro.append(DbKeyConfig.SCHOOL_LEVEL, profile.getSchoolLevel());
                pro.append(DbKeyConfig.SCHOOL_NAME, profile.getSchoolName());
                pro.append(DbKeyConfig.SCHOOL_ID, schoolId);
                pro.append(DbKeyConfig.MAJOR, profile.getMajor());
                pro.append(DbKeyConfig.RECENT_WORK_PLACE, profile.getRecentWorkPlace());
                pro.append(DbKeyConfig.DATE_OF_APPLY, profile.getDateOfApply());
                pro.append(DbKeyConfig.SOURCE_CV_NAME, profile.getSourceCVName());
                pro.append(DbKeyConfig.SOURCE_CV_ID, sourceCVId);
                pro.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
                pro.append(DbKeyConfig.CREATE_BY, info.getUsername());

                db.insertOne(CollectionNameDefs.COLL_PROFILE, pro);
            } finally {
                synchronized (queue) {
                    queue.removeIf(s -> s.getKey().equals(key));
                }
            }
        }

        response.setSuccess();
        return response;
    }

    public File convertToFile(MultipartFile file) throws IOException {
        File convFile = new File(serverPath + file.getOriginalFilename());
        if (!convFile.getPath().endsWith("xlsx") && !convFile.getPath().endsWith("xls")) {
            return null;
        }
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public boolean validateEmail(String email) {
        Matcher matcher = patternEmail.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onValidatorResult(String key, DictionaryNameValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}