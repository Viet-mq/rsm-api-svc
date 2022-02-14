package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.EventEntity;
import com.edso.resume.api.domain.entities.ProfileRabbitMQEntity;
import com.edso.resume.api.domain.entities.ProfileUploadEntity;
import com.edso.resume.api.domain.validator.DictionaryNameValidateProcessor;
import com.edso.resume.api.domain.validator.DictionaryNameValidatorResult;
import com.edso.resume.api.domain.validator.IDictionaryNameValidator;
import com.edso.resume.lib.common.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class UploadProfilesServiceImpl extends BaseService implements UploadProfilesService, IDictionaryNameValidator {

    public static final int COLUMN_TIME = 0;
    public static final int COLUMN_JOB_NAME = 1;
    public static final int COLUMN_FULL_NAME = 2;
    public static final int COLUMN_LINKEDIN = 3;
    public static final int COLUMN_FACEBOOK = 4;
    public static final int COLUMN_PHONE_NUMBER = 5;
    public static final int COLUMN_EMAIL = 6;
    public static final int COLUMN_SKYPE = 7;
    public static final int COLUMN_GITHUB = 8;
    public static final int COLUMN_OTHER_TECH = 9;
    public static final int COLUMN_LEVEL_JOB_NAME = 10;
    public static final int COLUMN_SOURCE_CV = 11;
    public static final int COLUMN_WEB = 12;
    public static final int COLUMN_PIC = 13;
    public static final int COLUMN_STATUS = 14;
    public static final int COLUMN_NOTE = 15;
    public static final int COLUMN_COMPANY = 16;

    private final RabbitTemplate rabbitTemplate;
    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingkey;
    @Value("${fileProfiles.fileSize}")
    private Long fileSize;

    private final Queue<DictionaryNameValidatorResult> queue = new LinkedBlockingQueue<>();

    @JsonIgnore
    protected HeaderInfo info;

    @Value("${excel.serverPath}")
    private String serverPath;

    public UploadProfilesServiceImpl(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate) {
        super(db);
        this.rabbitTemplate = rabbitTemplate;
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
            if (nextRow.getRowNum() < 1) {
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
                    case COLUMN_TIME:
                        profiles.setTime(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_JOB_NAME:
                        profiles.setJob(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_FULL_NAME:
                        profiles.setFullName(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_LINKEDIN:
                        profiles.setLinkedin(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_FACEBOOK:
                        profiles.setFacebook(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_PHONE_NUMBER:
                        profiles.setPhoneNumber(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_EMAIL:
                        profiles.setEmail(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_SKYPE:
                        profiles.setSkype(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_GITHUB:
                        profiles.setGithub(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_OTHER_TECH:
                        profiles.setOtherTech(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_LEVEL_JOB_NAME:
                        profiles.setLevel(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_SOURCE_CV:
                        profiles.setSourceCV(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_WEB:
                        profiles.setWeb(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_PIC:
                        profiles.setPic(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_STATUS:
                        profiles.setStatus(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_NOTE:
                        profiles.setNote(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    case COLUMN_COMPANY:
                        profiles.setCompany(AppUtils.mergeWhitespace((String) getCellValue(cell)));
                        break;
                    default:
                        break;
                }
            }
            //validate
            if (Strings.isNullOrEmpty(profiles.getFullName())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getJob())) {
                continue;
            }
            if (Strings.isNullOrEmpty(profiles.getSourceCV())) {
                continue;
            }
//            if (!AppUtils.validateFullName(profiles.getFullName())) {
//                logger.info("Họ và tên không đúng định dạng! fullName: {}", profiles.getFullName());
//                continue;
//            }
            if (!Strings.isNullOrEmpty(profiles.getEmail()) && !AppUtils.validateEmail(profiles.getEmail())) {
                logger.info("Email không đúng định dạng! email: {}", profiles.getEmail());
                continue;
            }
            if (!Strings.isNullOrEmpty(profiles.getPhoneNumber()) && !AppUtils.validatePhone(profiles.getPhoneNumber())) {
                logger.info("Số điện thoại không đúng định dạng! phoneNumber: {}", profiles.getPhoneNumber());
                continue;
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
        if (request != null && request.getSize() > fileSize) {
            return new BaseResponse(ErrorCodeDefs.FILE, "File vượt quá dung lượng cho phép");
        }
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
            Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));
            for (ProfileUploadEntity profile : profiles) {
                String key = UUID.randomUUID().toString();

                try {
                    List<DictionaryNameValidateProcessor> rs = new ArrayList<>();
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.SOURCE_CV, profile.getSourceCV(), db, this));
                    if (!Strings.isNullOrEmpty(profile.getEmail())) {
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.BLACKLIST_EMAIL, profile.getEmail(), db, this));
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.PROFILE_EMAIL, profile.getEmail(), db, this));
                    }
                    if (!Strings.isNullOrEmpty(profile.getPhoneNumber())) {
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.BLACKLIST_PHONE_NUMBER, profile.getPhoneNumber(), db, this));
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.PROFILE_PHONE_NUMBER, profile.getPhoneNumber(), db, this));
                    }
                    rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.JOB, profile.getJob(), db, this));
                    if (!Strings.isNullOrEmpty(profile.getLevel())) {
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.JOB_LEVEL, profile.getLevel(), db, this));
                    }
                    if (!Strings.isNullOrEmpty(profile.getPic())) {
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.PIC, profile.getPic(), db, this));
                    }
                    if (!Strings.isNullOrEmpty(profile.getCompany())) {
                        rs.add(new DictionaryNameValidateProcessor(key, ThreadConfig.COMPANY, profile.getCompany(), db, this));
                    }
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

                    String sourceCVId = null;
                    String jobLevelId = null;
                    String jobId = null;
                    String companyId = null;
                    String picId = null;

                    for (DictionaryNameValidateProcessor r : rs) {
                        switch (r.getResult().getType()) {
                            case ThreadConfig.SOURCE_CV: {
                                sourceCVId = r.getResult().getId();
                                break;
                            }
                            case ThreadConfig.JOB_LEVEL: {
                                jobLevelId = r.getResult().getId();
                                break;
                            }
                            case ThreadConfig.JOB: {
                                jobId = r.getResult().getId();
                                break;
                            }
                            case ThreadConfig.COMPANY: {
                                companyId = r.getResult().getId();
                                break;
                            }
                            case ThreadConfig.PIC: {
                                picId = r.getResult().getId();
                                break;
                            }
                        }
                    }

                    String color = randomColor();

                    String idProfile = UUID.randomUUID().toString();
                    Document pro = new Document();
                    pro.append(DbKeyConfig.ID, idProfile);
                    pro.append(DbKeyConfig.TIME, parseMillis(profile.getTime()));
                    pro.append(DbKeyConfig.JOB_ID, jobId);
                    pro.append(DbKeyConfig.JOB_NAME, profile.getJob());
                    pro.append(DbKeyConfig.FULL_NAME, profile.getFullName());
                    pro.append(DbKeyConfig.LINKEDIN, profile.getLinkedin());
                    pro.append(DbKeyConfig.FACEBOOK, profile.getFacebook());
                    pro.append(DbKeyConfig.PHONE_NUMBER, profile.getPhoneNumber());
                    pro.append(DbKeyConfig.EMAIL, profile.getEmail());
                    pro.append(DbKeyConfig.SKYPE, profile.getSkype());
                    pro.append(DbKeyConfig.GITHUB, profile.getGithub());
                    pro.append(DbKeyConfig.OTHER_TECH, profile.getOtherTech());
                    pro.append(DbKeyConfig.LEVEL_JOB_ID, jobLevelId);
                    pro.append(DbKeyConfig.LEVEL_JOB_NAME, profile.getLevel());
                    pro.append(DbKeyConfig.SOURCE_CV_ID, sourceCVId);
                    pro.append(DbKeyConfig.SOURCE_CV_NAME, profile.getSourceCV());
                    pro.append(DbKeyConfig.WEB, profile.getWeb());
                    pro.append(DbKeyConfig.PIC_ID, picId);
                    pro.append(DbKeyConfig.PIC_NAME, profile.getPic());
                    pro.append(DbKeyConfig.STATUS, profile.getStatus());
                    pro.append(DbKeyConfig.COMPANY_ID, companyId);
                    pro.append(DbKeyConfig.COMPANY_NAME, profile.getCompany());
                    pro.append(DbKeyConfig.NAME_SEARCH, AppUtils.parseVietnameseToEnglish(profile.getFullName()));
                    pro.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
                    pro.append(DbKeyConfig.CREATE_BY, info.getUsername());
                    pro.append(DbKeyConfig.AVATAR_COLOR, color);

                    db.insertOne(CollectionNameDefs.COLL_PROFILE, pro);

                    Document comment = new Document();
                    comment.append(DbKeyConfig.ID, UUID.randomUUID().toString());
                    comment.append(DbKeyConfig.ID_PROFILE, idProfile);
                    comment.append(DbKeyConfig.CONTENT, AppUtils.mergeWhitespace(profile.getNote()));
                    comment.append(DbKeyConfig.CREATE_AT, System.currentTimeMillis());
                    comment.append(DbKeyConfig.CREATE_BY, info.getUsername());
                    comment.append(DbKeyConfig.FULL_NAME, user.get(DbKeyConfig.FULL_NAME));

                    db.insertOne(CollectionNameDefs.COLL_COMMENT, comment);

                    ProfileRabbitMQEntity profileEntity = new ProfileRabbitMQEntity();
                    profileEntity.setId(idProfile);
                    profileEntity.setTime(parseMillis(profile.getTime()));
                    profileEntity.setJobId(jobId);
                    profileEntity.setJobName(profile.getJob());
                    profileEntity.setFullName(profile.getFullName());
                    profileEntity.setLinkedin(profile.getLinkedin());
                    profileEntity.setFacebook(profile.getFacebook());
                    profileEntity.setPhoneNumber(profile.getPhoneNumber());
                    profileEntity.setEmail(profile.getEmail());
                    profileEntity.setSkype(profile.getSkype());
                    profileEntity.setGithub(profile.getGithub());
                    profileEntity.setOtherTech(profile.getOtherTech());
                    profileEntity.setLevelJobId(jobLevelId);
                    profileEntity.setLevelJobName(profile.getLevel());
                    profileEntity.setSourceCVId(sourceCVId);
                    profileEntity.setSourceCVName(profile.getSourceCV());
                    profileEntity.setWeb(profile.getWeb());
                    profileEntity.setPicId(picId);
                    profileEntity.setPicName(profile.getPic());
                    profileEntity.setStatus(profile.getStatus());
                    profileEntity.setCompanyId(companyId);
                    profileEntity.setCompanyName(profile.getCompany());
                    profileEntity.setAvatarColor(color);
                    publishActionToRabbitMQ(profileEntity);
                } finally {
                    synchronized (queue) {
                        queue.removeIf(s -> s.getKey().equals(key));
                    }
                }
            }
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
        response.setSuccess();
        return response;
    }


    public Long parseMillis(String date) {
        try {
            if (Strings.isNullOrEmpty(date)) {
                return null;
            }
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            return format.parse(date).getTime();
        } catch (Throwable ex) {
            logger.error("Ex: ", ex);
            return null;
        }
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

    @Override
    public void onValidatorResult(String key, DictionaryNameValidatorResult result) {
        result.setKey(key);
        queue.offer(result);
    }
}
