package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.*;
import com.edso.resume.api.domain.rabbitmq.config.RabbitMQOnlineActions;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.edso.resume.lib.common.TypeConfig;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.entities.PagingInfo;
import com.edso.resume.lib.response.GetArrayResponse;
import com.google.common.base.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class HistoryEmailServiceImpl extends BaseService implements HistoryEmailService {

    private final RabbitMQOnlineActions rabbit;
    private final SendEmailService sendRejectEmailToCandidate;
    private final SendEmailService sendRejectEmailToPresenter;
    private final SendEmailService sendRejectEmailToRelatedPeople;
    private final SendEmailService sendRejectEmailToInterviewer;
    private final SendEmailService sendRoundEmailToPresenter;
    private final SendEmailService sendRoundEmailToCandidate;
    private final SendEmailService sendRoundEmailToInterviewer;
    private final SendEmailService sendRoundEmailToRelatedPeople;
    private final SendEmailService sendCalendarEmailToPresenter;
    private final SendEmailService sendCalendarEmailToInterviewer;
    private final SendEmailService sendCalenderEmailToCandidate;
    private final SendEmailService sendCalenderEmailToRelatedPeople;


    @Value("${mail.serverPath}")
    private String serverPath;

    protected HistoryEmailServiceImpl(MongoDbOnlineSyncActions db,
                                      RabbitMQOnlineActions rabbit,
                                      @Qualifier("sendRejectEmailToCandidateService") SendEmailService sendRejectEmailToCandidate,
                                      @Qualifier("sendRejectEmailToPresenterService") SendEmailService sendRejectEmailToPresenter,
                                      @Qualifier("sendRejectEmailToRelatedPeopleService") SendEmailService sendRejectEmailToRelatedPeople,
                                      @Qualifier("sendRejectEmailToInterviewerService") SendEmailService sendRejectEmailToInterviewer,
                                      @Qualifier("sendRoundEmailToPresenterService") SendEmailService sendRoundEmailToPresenter,
                                      @Qualifier("sendRoundEmailToCandidateService") SendEmailService sendRoundEmailToCandidate,
                                      @Qualifier("sendRoundEmailToInterviewerService") SendEmailService sendRoundEmailToInterviewer,
                                      @Qualifier("sendRoundEmailToRelatedPeopleService") SendEmailService sendRoundEmailToRelatedPeople,
                                      @Qualifier("sendCalendarEmailToPresenterService") SendEmailService sendCalendarEmailToPresenter,
                                      @Qualifier("sendCalendarEmailToInterviewerService") SendEmailService sendCalendarEmailToInterviewer,
                                      @Qualifier("sendCalendarEmailToCandidateService") SendEmailService sendCalenderEmailToCandidate,
                                      @Qualifier("sendCalendarEmailToRelatedPeopleService") SendEmailService sendCalenderEmailToRelatedPeople) {
        super(db);
        this.rabbit = rabbit;
        this.sendRejectEmailToCandidate = sendRejectEmailToCandidate;
        this.sendRejectEmailToPresenter = sendRejectEmailToPresenter;
        this.sendRejectEmailToRelatedPeople = sendRejectEmailToRelatedPeople;
        this.sendRejectEmailToInterviewer = sendRejectEmailToInterviewer;
        this.sendRoundEmailToPresenter = sendRoundEmailToPresenter;
        this.sendRoundEmailToCandidate = sendRoundEmailToCandidate;
        this.sendRoundEmailToInterviewer = sendRoundEmailToInterviewer;
        this.sendRoundEmailToRelatedPeople = sendRoundEmailToRelatedPeople;
        this.sendCalendarEmailToPresenter = sendCalendarEmailToPresenter;
        this.sendCalendarEmailToInterviewer = sendCalendarEmailToInterviewer;
        this.sendCalenderEmailToCandidate = sendCalenderEmailToCandidate;
        this.sendCalenderEmailToRelatedPeople = sendCalenderEmailToRelatedPeople;
    }


    @Override
    public void createHistoryEmail(String type, String profileId, List<String> usernames, String email, String subject, String content, List<MultipartFile> files, HeaderInfo info) throws IOException, TimeoutException {
        List<String> listPath = new ArrayList<>();
        List<Document> listDocument = new ArrayList<>();
        Document fullName = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String path = AppUtils.saveFile(serverPath, file);
                    listPath.add(path);
                    Document pathDocument = new Document();
                    pathDocument.append(DbKeyConfig.FILE_NAME, file.getOriginalFilename());
                    pathDocument.append(DbKeyConfig.PATH_FILE, path);
                    listDocument.add(pathDocument);
                }
            }
        }

        List<String> emails = splitString(AppUtils.removeWhitespace(email), ";");

        SendEmailService service = getEmailService(type);
        List<EmailResult> list = new ArrayList<>();
        if (service != null) {
            list = service.sendEmail(profileId, usernames, emails, subject, content);
        }

        for (EmailResult emailResult : list) {
            String historyId = UUID.randomUUID().toString();
            Document history = new Document();
            history.append(DbKeyConfig.ID, historyId);
            history.append(DbKeyConfig.TYPE, getType(type));
            history.append(DbKeyConfig.ID_PROFILE, profileId);
            history.append(DbKeyConfig.EMAIL, emailResult.getEmail());
            history.append(DbKeyConfig.SUBJECT, emailResult.getSubject());
            history.append(DbKeyConfig.CONTENT, emailResult.getContent());
            history.append(DbKeyConfig.TIME, System.currentTimeMillis());
            history.append(DbKeyConfig.USERNAME, info.getUsername());
            if (!Strings.isNullOrEmpty(emailResult.getEmail())) {
                history.append(DbKeyConfig.STATUS, "Đang đợi gửi");
            } else {
                history.append(DbKeyConfig.STATUS, "Không thể gửi vì không có email");
            }
            history.append(DbKeyConfig.FULL_NAME, fullName.get(DbKeyConfig.FULL_NAME));
            history.append(DbKeyConfig.FILE, listDocument);

            // insert to database
            db.insertOne(CollectionNameDefs.COLL_HISTORY_EMAIL, history);
            logger.info("createHistoryEmail history: {}", history);

            if (!Strings.isNullOrEmpty(emailResult.getEmail())) {
                EmailEntity emailEntity = EmailEntity.builder()
                        .type(type)
                        .historyId(historyId)
                        .email(emailResult.getEmail())
                        .subject(emailResult.getSubject())
                        .content(emailResult.getContent())
                        .files(listPath)
                        .build();
                rabbit.publishEmails(emailEntity);
            }
        }
    }

    @Override
    public void createHistoryEmails(String type, List<IdEntity> ids, List<String> usernames, String email, String subject, String content, List<MultipartFile> files, HeaderInfo info) throws IOException, TimeoutException {
        List<String> listPath = new ArrayList<>();
        List<Document> listDocument = new ArrayList<>();
        Document fullName = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, info.getUsername()));

        List<String> emails = splitString(AppUtils.removeWhitespace(email), ";");

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String path = AppUtils.saveFile(serverPath, file);
                    listPath.add(path);
                    Document pathDocument = new Document();
                    pathDocument.append(DbKeyConfig.FILE_NAME, file.getOriginalFilename());
                    pathDocument.append(DbKeyConfig.PATH_FILE, path);
                    listDocument.add(pathDocument);
                }
            }
        }

        for (IdEntity id : ids) {

            SendEmailService service = getEmailService(type);
            List<EmailResult> list = new ArrayList<>();
            if (service != null) {
                list = service.sendCalendarEmail(id.getCalendarId(), usernames, emails, subject, content);
            }

            for (EmailResult emailResult : list) {
                String historyId = UUID.randomUUID().toString();
                id.setHistoryId(historyId);
                Document history = new Document();
                history.append(DbKeyConfig.ID, historyId);
                history.append(DbKeyConfig.TYPE, getType(type));
                history.append(DbKeyConfig.ID_PROFILE, id.getProfileId());
                history.append(DbKeyConfig.EMAIL, emailResult.getEmail());
                history.append(DbKeyConfig.SUBJECT, emailResult.getSubject());
                history.append(DbKeyConfig.CONTENT, emailResult.getContent());
                history.append(DbKeyConfig.TIME, System.currentTimeMillis());
                history.append(DbKeyConfig.USERNAME, info.getUsername());
                if (!Strings.isNullOrEmpty(emailResult.getEmail())) {
                    history.append(DbKeyConfig.STATUS, "Đang đợi gửi");
                } else {
                    history.append(DbKeyConfig.STATUS, "Không thể gửi vì không có email");
                }
                history.append(DbKeyConfig.FULL_NAME, fullName.get(DbKeyConfig.FULL_NAME));
                history.append(DbKeyConfig.FILE, listDocument);
                history.append(DbKeyConfig.ORGANIZATIONS, info.getMyOrganizations());

                // insert to database
                db.insertOne(CollectionNameDefs.COLL_HISTORY_EMAIL, history);
                logger.info("createHistoryEmails history: {}", history);

                if (!Strings.isNullOrEmpty(emailResult.getEmail())) {
                    EmailEntity emailEntity = EmailEntity.builder()
                            .type(type)
                            .historyId(historyId)
                            .email(emailResult.getEmail())
                            .subject(emailResult.getSubject())
                            .content(emailResult.getContent())
                            .files(listPath)
                            .build();
                    rabbit.publishEmails(emailEntity);
                }
            }
        }
    }

    private String getType(String type) {
        switch (type) {
            case TypeConfig.CALENDAR_INTERVIEWER:
            case TypeConfig.CALENDARS_INTERVIEWER: {
                return "Hội đồng tuyển dụng";
            }
            case TypeConfig.REJECT_CANDIDATE:
            case TypeConfig.ROUND_CANDIDATE:
            case TypeConfig.CALENDAR_CANDIDATE:
            case TypeConfig.CALENDARS_CANDIDATE: {
                return "Ứng viên";
            }
            case TypeConfig.REJECT_PRESENTER:
            case TypeConfig.ROUND_PRESENTER:
            case TypeConfig.CALENDAR_PRESENTER:
            case TypeConfig.CALENDARS_PRESENTER: {
                return "Người giới thiệu";
            }
            case TypeConfig.REJECT_RELATED_PEOPLE:
            case TypeConfig.ROUND_RELATED_PEOPLE:
            case TypeConfig.CALENDAR_RELATED_PEOPLE:
            case TypeConfig.CALENDARS_RELATED_PEOPLE: {
                return "Người liên quan";
            }
        }
        return null;
    }

    private SendEmailService getEmailService(String type) {
        switch (type) {
            case TypeConfig.CALENDAR_INTERVIEWER:
            case TypeConfig.CALENDARS_INTERVIEWER: {
                return sendCalendarEmailToInterviewer;
            }
            case TypeConfig.CALENDAR_CANDIDATE:
            case TypeConfig.CALENDARS_CANDIDATE: {
                return sendCalenderEmailToCandidate;
            }
            case TypeConfig.CALENDAR_PRESENTER:
            case TypeConfig.CALENDARS_PRESENTER: {
                return sendCalendarEmailToPresenter;
            }
            case TypeConfig.CALENDAR_RELATED_PEOPLE:
            case TypeConfig.CALENDARS_RELATED_PEOPLE: {
                return sendCalenderEmailToRelatedPeople;
            }
            case TypeConfig.REJECT_CANDIDATE: {
                return sendRejectEmailToCandidate;
            }
            case TypeConfig.REJECT_PRESENTER: {
                return sendRejectEmailToPresenter;
            }
            case TypeConfig.REJECT_INTERVIEWER: {
                return sendRejectEmailToInterviewer;
            }
            case TypeConfig.REJECT_RELATED_PEOPLE: {
                return sendRejectEmailToRelatedPeople;
            }
            case TypeConfig.ROUND_CANDIDATE: {
                return sendRoundEmailToCandidate;
            }
            case TypeConfig.ROUND_PRESENTER: {
                return sendRoundEmailToPresenter;
            }
            case TypeConfig.ROUND_RELATED_PEOPLE: {
                return sendRoundEmailToRelatedPeople;
            }
            case TypeConfig.ROUND_INTERVIEWER: {
                return sendRoundEmailToInterviewer;
            }
        }
        return null;
    }

    @Override
    public void deleteHistoryEmail(String idProfile) {
        db.delete(CollectionNameDefs.COLL_HISTORY_EMAIL, Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        logger.info("deleteHistoryEmail idProfile: {}", idProfile);
    }

    @Override
    public GetArrayResponse<HistoryEmailEntity> findAllHistoryEmail(HeaderInfo info, String idProfile, Integer page, Integer size) {
        GetArrayResponse<HistoryEmailEntity> resp = new GetArrayResponse<>();

        Bson con = Filters.eq(DbKeyConfig.ID, idProfile);
        Document idProfileDocument = db.findOne(CollectionNameDefs.COLL_PROFILE, con);

        if (idProfileDocument == null) {
            resp.setFailed("Id profile không tồn tại");
            return resp;
        }

        List<Bson> c = new ArrayList<>();
        c.add(Filters.eq(DbKeyConfig.ID_PROFILE, idProfile));
        c.add(Filters.in(DbKeyConfig.ORGANIZATIONS, info.getOrganizations()));
        Bson cond = buildCondition(c);
        Bson sort = Filters.eq(DbKeyConfig.TIME, -1);

        PagingInfo pagingInfo = PagingInfo.parse(page, size);
        FindIterable<Document> lst = db.findAll2(CollectionNameDefs.COLL_HISTORY_PROFILE, cond, sort, pagingInfo.getStart(), pagingInfo.getLimit());
        List<HistoryEmailEntity> rows = new ArrayList<>();
        if (lst != null) {
            for (Document doc : lst) {
                List<FileEntity> list = new ArrayList<>();
                List<Document> documentList = (List<Document>) doc.get(DbKeyConfig.FILE);
                if (documentList != null && !documentList.isEmpty()) {
                    for (Document document : documentList) {
                        FileEntity file = FileEntity.builder()
                                .fileName(AppUtils.parseString(document.get(DbKeyConfig.FILE_NAME)))
                                .filePath(AppUtils.parseString(document.get(DbKeyConfig.PATH_FILE)))
                                .build();
                        list.add(file);
                    }
                }

                HistoryEmailEntity history = HistoryEmailEntity.builder()
                        .id(AppUtils.parseString(doc.get(DbKeyConfig.ID)))
                        .type(AppUtils.parseString(doc.get(DbKeyConfig.TYPE)))
                        .idProfile(AppUtils.parseString(doc.get(DbKeyConfig.ID_PROFILE)))
                        .email(AppUtils.parseString(doc.get(DbKeyConfig.EMAIL)))
                        .subject(AppUtils.parseString(doc.get(DbKeyConfig.SUBJECT)))
                        .content(AppUtils.parseString(doc.get(DbKeyConfig.CONTENT)))
                        .status(AppUtils.parseString(doc.get(DbKeyConfig.STATUS)))
                        .time(AppUtils.parseLong(doc.get(DbKeyConfig.TIME)))
                        .username(AppUtils.parseString(doc.get(DbKeyConfig.USERNAME)))
                        .fullName(AppUtils.parseString(doc.get(DbKeyConfig.FULL_NAME)))
                        .files(list)
                        .build();
                rows.add(history);
            }
        }

        resp.setSuccess();
        resp.setTotal(db.countAll(CollectionNameDefs.COLL_HISTORY_EMAIL, cond));
        resp.setRows(rows);
        return resp;
    }
}
