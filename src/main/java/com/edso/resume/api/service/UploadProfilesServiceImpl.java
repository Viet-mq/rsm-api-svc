package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.EventEntity;
import com.edso.resume.api.domain.entities.ProfileRabbitMQEntity;
import com.edso.resume.api.domain.entities.ProfileUploadEntity;
import com.edso.resume.api.domain.validator.DictionaryNameValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryNameValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryNameValidator;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.NameConfig;
import com.edso.resume.lib.common.ThreadConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static final int COLUMN_LEVEL_SCHOOL = 6;
    public static final int COLUMN_SCHOOL_NAME = 7;
    public static final int COLUMN_LEVEL_JOB_NAME = 8;
    public static final int COLUMN_DATE_OF_APPLY = 9;
    public static final int COLUMN_SOURCE_CV = 10;
    public static final int COLUMN_TALENT_POOL = 11;
    public static final int COLUMN_HR_REFERENCE = 12;
    public static final int COLUMN_DEPARTMENT = 13;

    private final RabbitTemplate rabbitTemplate;
    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;

    private final Queue<DictionaryNameValidatorResult> queue = new LinkedBlockingQueue<>();
    private static final String EMAIL_REGEX = "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
    private static final String FULL_NAME_REGEX = "^[\\p{L} .'-]+$";
    private static Pattern patternEmail;
    private static Pattern fullNamePattern;
    private static Matcher matcher;

    @JsonIgnore
    protected HeaderInfo info;

    @Value("${excel.serverPath}")
    private String serverPath;

    public UploadProfilesServiceImpl(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate) {
        super(db);
        this.rabbitTemplate = rabbitTemplate;
        patternEmail = Pattern.compile(EMAIL_REGEX);
        fullNamePattern = Pattern.compile(FULL_NAME_REGEX);
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

    private List<ProfileUploadEntity> readExcel(MultipartFile excelFile) throws IOException {
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
                    case COLUMN_LEVEL_SCHOOL:
                        profiles.setLevelSchool((String) getCellValue(cell));
                        break;
                    case COLUMN_SCHOOL_NAME:
                        profiles.setSchoolName((String) getCellValue(cell));
                        break;
                    case COLUMN_LEVEL_JOB_NAME:
                        profiles.setLevelJobName((String) getCellValue(cell));
                        break;
                    case COLUMN_DATE_OF_APPLY:
                        profiles.setDateOfApply((String) getCellValue(cell));
                        break;
                    case COLUMN_SOURCE_CV:
                        profiles.setSourceCVName((String) getCellValue(cell));
                        break;
                    case COLUMN_TALENT_POOL:
                        profiles.setTalentPoolName((String) getCellValue(cell));
                        break;
                    case COLUMN_HR_REFERENCE:
                        profiles.setHrRef((String) getCellValue(cell));
                        break;
                    case COLUMN_DEPARTMENT:
                        profiles.setDepartmentName((String) getCellValue(cell));
                        break;
                    default:
                        break;
                }
            }
            //validate
            if (Strings.isNullOrEmpty(profiles.getFullName())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getPhoneNumber())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getEmail())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getLevelJobName())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getDateOfApply())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getSourceCVName())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getTalentPoolName())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getDepartmentName())) {
                continue;
            }
            if (!validateFullName(profiles.getFullName())) {
                logger.info("Họ và tên không đúng định dạng! fullName: {}", profiles.getFullName());
                continue;
            }
            if (!validateEmail(profiles.getEmail())) {
                logger.info("Email không đúng định dạng! email: {}", profiles.getEmail());
                continue;
            }
            if (!Strings.isNullOrEmpty(profiles.getGender())) {
                if (!profiles.getGender().equals(NameConfig.NU) && !profiles.getGender().equals(NameConfig.NAM)) {
                    logger.info("Giới tính chỉ có thể là Nam hoặc Nữ!");
                    continue;
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
        try {
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
                String key = UUID.randomUUID().toString();

                try {
                    List<DictionaryNameValidateProcessor> rs = new ArrayList<>();
                    if (!Strings.isNullOrEmpty(profile.getSchoolName())) {
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.SCHOOL, profile.getSchoolName(), db, this));
                    }
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.SOURCE_CV, profile.getSourceCVName(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, profile.getEmail(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, profile.getPhoneNumber(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, profile.getEmail(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, profile.getPhoneNumber(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.TALENT_POOL, profile.getTalentPoolName(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.JOB_LEVEL, profile.getLevelJobName(), db, this));
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.DEPARTMENT, profile.getDepartmentName(), db, this));

                    int total = rs.size();

                    for (DictionaryNameValidateProcessor r : rs) {
                        Thread t = new Thread(r);
                        t.start();
                    }

                    long time = System.currentTimeMillis();
                    int count = 0;
                    while (total > 0 && (time + 30000 > System.currentTimeMillis())) {
                        DictionaryNameValidatorResult validatorResult = queue.poll();
                        if (validatorResult != null) {
                            if (validatorResult.getKey().equals(key)) {
                                if (!validatorResult.isResult()) {
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

                    if (count != rs.size()) {
                        continue;
                    }

                    String schoolId = null;
                    String sourceCVId = null;
                    String talentPoolId = null;
                    String levelJobId = null;
                    String departmentId = null;

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
                            case ThreadConfig.TALENT_POOL: {
                                talentPoolId = r.getResult().getId();
                                break;
                            }
                            case ThreadConfig.JOB_LEVEL: {
                                levelJobId = r.getResult().getId();
                                break;
                            }
                            case ThreadConfig.DEPARTMENT: {
                                departmentId = r.getResult().getId();
                                break;
                            }
                        }
                    }

                    Long dateOfBirth = null;
                    Long dateOfApply;
                    try {
                        dateOfApply = parseMillis(profile.getDateOfApply());
                    } catch (Throwable e) {
                        logger.error("Exception: ", e);
                        continue;
                    }
                    if (!Strings.isNullOrEmpty(profile.getDateOfBirth())) {
                        try {
                            dateOfBirth = parseMillis(profile.getDateOfBirth());
                        } catch (Throwable e) {
                            logger.error("Exception: ", e);
                            continue;
                        }
                    }

                    String idProfile = UUID.randomUUID().toString();
                    Document pro = new Document();
                    pro.append(DbKeyConfig.ID, idProfile);
                    pro.append(DbKeyConfig.FULL_NAME, profile.getFullName());
                    pro.append(DbKeyConfig.PHONE_NUMBER, profile.getPhoneNumber());
                    pro.append(DbKeyConfig.EMAIL, profile.getEmail());
                    pro.append(DbKeyConfig.DATE_OF_BIRTH, dateOfBirth);
                    pro.append(DbKeyConfig.GENDER, profile.getGender());
                    pro.append(DbKeyConfig.HOMETOWN, profile.getHometown());
                    pro.append(DbKeyConfig.LEVEL_SCHOOL, profile.getLevelSchool());
                    pro.append(DbKeyConfig.SCHOOL_NAME, profile.getSchoolName());
                    pro.append(DbKeyConfig.SCHOOL_ID, schoolId);
                    pro.append(DbKeyConfig.DATE_OF_APPLY, dateOfApply);
                    pro.append(DbKeyConfig.LEVEL_JOB_ID, levelJobId);
                    pro.append(DbKeyConfig.LEVEL_JOB_NAME, profile.getLevelJobName());
                    pro.append(DbKeyConfig.SOURCE_CV_NAME, profile.getSourceCVName());
                    pro.append(DbKeyConfig.SOURCE_CV_ID, sourceCVId);
                    pro.append(DbKeyConfig.NAME_SEARCH, parseVietnameseToEnglish(profile.getFullName()));
                    pro.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
                    pro.append(DbKeyConfig.CREATE_BY, info.getUsername());
                    pro.append(DbKeyConfig.TALENT_POOL_ID, talentPoolId);
                    pro.append(DbKeyConfig.TALENT_POOL_NAME, profile.getTalentPoolName());
                    pro.append(DbKeyConfig.HR_REF, profile.getHrRef());
                    pro.append(DbKeyConfig.DEPARTMENT_ID, departmentId);
                    pro.append(DbKeyConfig.DEPARTMENT_NAME, profile.getDepartmentName());

                    db.insertOne(CollectionNameDefs.COLL_PROFILE, pro);

                    ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
                    profileEntity.setId(idProfile);
                    profileEntity.setFullName(profile.getFullName());
                    profileEntity.setGender(profile.getGender());
                    profileEntity.setPhoneNumber(profile.getPhoneNumber());
                    profileEntity.setEmail(profile.getEmail());
                    profileEntity.setDateOfBirth(dateOfBirth);
                    profileEntity.setHometown(profile.getHometown());
                    profileEntity.setSchoolId(schoolId);
                    profileEntity.setSchoolName(profile.getSchoolName());
                    profileEntity.setDateOfApply(dateOfApply);
                    profileEntity.setSourceCVId(sourceCVId);
                    profileEntity.setLevelSchool(profile.getLevelSchool());
                    profileEntity.setLevelJobId(levelJobId);
                    profileEntity.setLevelJobName(profile.getLevelJobName());
                    profileEntity.setSourceCVName(profile.getSourceCVName());
                    profileEntity.setTalentPoolId(talentPoolId);
                    profileEntity.setTalentPoolName(profile.getTalentPoolName());
                    profileEntity.setHrRef(profile.getHrRef());
                    profileEntity.setDepartmentId(departmentId);
                    profileEntity.setDepartmentName(profile.getDepartmentName());

                    publishActionToRabbitMQ(profileEntity);
                } finally {
                    synchronized (queue) {
                        queue.removeIf(s -> s.getKey().equals(key));
                    }
                }
            }
        }catch (Throwable ex){
            logger.error("Exception: ", ex);
        }
        response.setSuccess();
        return response;
    }


    public Long parseMillis(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.parse(date).getTime();
    }

    private void publishActionToRabbitMQ(Object obj) {
        EventEntity event = new EventEntity("create", obj);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
        logger.info("=>publishActionToRabbitMQ type: {create}, profile: {}", obj);
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
        matcher = patternEmail.matcher(email);
        return matcher.matches();
    }

    public boolean validateFullName(String fullName) {
        matcher = fullNamePattern.matcher(fullName);
        return matcher.matches();
    }

    @Override
    public void onValidatorResult(String key, DictionaryNameValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}
