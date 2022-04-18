package com.edso.resume.api.service;

import com.edso.resume.api.common.EmailTemplateConfig;
import com.edso.resume.api.common.KeyPointConfig;
import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.EmailResult;
import com.edso.resume.lib.common.AppUtils;
import com.edso.resume.lib.common.CollectionNameDefs;
import com.edso.resume.lib.common.DbKeyConfig;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import lombok.SneakyThrows;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SendCalendarEmailToInterviewerService extends BaseService implements SendEmailService {

    protected SendCalendarEmailToInterviewerService(MongoDbOnlineSyncActions db) {
        super(db);
    }


    @Override
    public List<EmailResult> sendEmail(String profileId, List<String> usernames, List<String> emails, String subject, String content) {
        return null;
    }

    @SneakyThrows
    @Override
    public List<EmailResult> sendCalendarEmail(String calendarId, List<String> usernames, List<String> emails, String subject, String content) {
        List<EmailResult> results = new ArrayList<>();
        try {
            Document calendar = db.findOne(CollectionNameDefs.COLL_CALENDAR_PROFILE, Filters.eq(DbKeyConfig.ID, calendarId));

            String id_profile = AppUtils.parseString(calendar.get(DbKeyConfig.ID_PROFILE));
            Document profile = db.findOne(CollectionNameDefs.COLL_PROFILE, Filters.eq(EmailTemplateConfig.ID, id_profile));

            //Name Filter
            String fullName = AppUtils.parseString(profile.get(DbKeyConfig.FULL_NAME));
            String lastName;
            String firstName = null;
            if (fullName.split("\\w+").length > 1) {
                firstName = fullName.substring(fullName.lastIndexOf(" ") + 1);
                lastName = fullName.substring(0, fullName.indexOf(" "));
            } else {
                lastName = fullName;
            }

            if (usernames != null) {
                for (String username : usernames) {

                    Document user = db.findOne(CollectionNameDefs.COLL_USER, Filters.eq(DbKeyConfig.USERNAME, username));
                    if (user != null) {

                        //Get all keypoint in content and subject
                        final Pattern pattern = Pattern.compile(AppUtils.KEYPOINT_PATTERN);
                        Matcher keypointMatcher = pattern.matcher(content);
                        final List<String> keypointList = new LinkedList<>();
                        while (keypointMatcher.find()) {
                            keypointList.add(keypointMatcher.group(1));
                        }

                        keypointMatcher = pattern.matcher(subject);
                        while (keypointMatcher.find()) {
                            keypointList.add(keypointMatcher.group(1));
                        }

                        Map<String, String> replacementStrings = new HashMap<>();
                        for (String placeholder : keypointList) {
                            switch (placeholder) {
                                case KeyPointConfig.FULL_NAME:
                                    replacementStrings.put(KeyPointConfig.FULL_NAME, fullName);
                                    break;
                                case KeyPointConfig.NAME:
                                case KeyPointConfig.FIRST_NAME:
                                    replacementStrings.put(KeyPointConfig.NAME, firstName);
                                    break;
                                case KeyPointConfig.LAST_NAME:
                                    replacementStrings.put(KeyPointConfig.LAST_NAME, lastName);
                                    break;
                                case KeyPointConfig.EMAIL:
                                    replacementStrings.put(KeyPointConfig.EMAIL, AppUtils.parseString(profile.get(DbKeyConfig.EMAIL)));
                                    break;
                                case KeyPointConfig.JOB:
                                    replacementStrings.put(KeyPointConfig.JOB, AppUtils.parseString(profile.get(DbKeyConfig.JOB_NAME)));
                                    break;
                                case KeyPointConfig.COMPANY:
                                    break;
                                case KeyPointConfig.ROUND:
                                    replacementStrings.put(KeyPointConfig.ROUND, AppUtils.parseString(profile.get(DbKeyConfig.STATUS_CV_NAME)));
                                    break;
                                case KeyPointConfig.DATE:
                                    String date = AppUtils.formatDateToString(new Date(AppUtils.parseLong(calendar.get(DbKeyConfig.DATE))));
                                    replacementStrings.put(KeyPointConfig.DATE, date);
                                    break;
                                case KeyPointConfig.INTERVIEW_TIME:
                                    String interview_time = AppUtils.formatDateToString(new Date(AppUtils.parseLong(calendar.get(DbKeyConfig.INTERVIEW_TIME))));
                                    replacementStrings.put(KeyPointConfig.INTERVIEW_TIME, interview_time);
                                    break;
                                case KeyPointConfig.INTERVIEW_ADDRESS:
                                    replacementStrings.put(KeyPointConfig.INTERVIEW_ADDRESS, AppUtils.parseString(calendar.get(DbKeyConfig.INTERVIEW_ADDRESS_NAME)));
                                    break;
                                case KeyPointConfig.FLOOR:
                                    replacementStrings.put(KeyPointConfig.FLOOR, AppUtils.parseString(calendar.get(DbKeyConfig.FLOOR)));
                                    break;
                                case KeyPointConfig.INTERVIEW_TYPE:
                                    replacementStrings.put(KeyPointConfig.INTERVIEW_TYPE, AppUtils.parseString(calendar.get(DbKeyConfig.TYPE)));
                                    break;
                                case KeyPointConfig.INTERVIEWER_NAME:
                                    replacementStrings.put(KeyPointConfig.INTERVIEWER_NAME, AppUtils.parseString(user.get(DbKeyConfig.FULL_NAME)));
                            }
                        }

                        //Replace keypoint
                        StrSubstitutor sub = new StrSubstitutor(replacementStrings, "{", "}");
                        EmailResult emailResult = EmailResult.builder()
                                .email(AppUtils.parseString(user.get(DbKeyConfig.EMAIL)))
                                .subject(sub.replace(subject))
                                .content(sub.replace(content))
                                .build();
                        results.add(emailResult);
                    }
                }
            }
            if (emails != null) {
                for (String email : emails) {

                    //Get all keypoint in content and subject
                    final Pattern pattern = Pattern.compile(AppUtils.KEYPOINT_PATTERN);
                    Matcher keypointMatcher = pattern.matcher(content);
                    final List<String> keypointList = new LinkedList<>();
                    while (keypointMatcher.find()) {
                        keypointList.add(keypointMatcher.group(1));
                    }

                    keypointMatcher = pattern.matcher(subject);
                    while (keypointMatcher.find()) {
                        keypointList.add(keypointMatcher.group(1));
                    }

                    Map<String, String> replacementStrings = new HashMap<>();
                    for (String placeholder : keypointList) {
                        switch (placeholder) {
                            case KeyPointConfig.FULL_NAME:
                                replacementStrings.put(KeyPointConfig.FULL_NAME, email);
                                break;
                            case KeyPointConfig.NAME:
                            case KeyPointConfig.FIRST_NAME:
                                replacementStrings.put(KeyPointConfig.NAME, firstName);
                                break;
                            case KeyPointConfig.LAST_NAME:
                                replacementStrings.put(KeyPointConfig.LAST_NAME, lastName);
                                break;
                            case KeyPointConfig.EMAIL:
                                replacementStrings.put(KeyPointConfig.EMAIL, AppUtils.parseString(profile.get(DbKeyConfig.EMAIL)));
                                break;
                            case KeyPointConfig.JOB:
                                replacementStrings.put(KeyPointConfig.JOB, AppUtils.parseString(profile.get(DbKeyConfig.JOB_NAME)));
                                break;
                            case KeyPointConfig.COMPANY:
                                break;
                            case KeyPointConfig.ROUND:
                                replacementStrings.put(KeyPointConfig.ROUND, AppUtils.parseString(profile.get(DbKeyConfig.STATUS_CV_NAME)));
                                break;
                            case KeyPointConfig.DATE:
                                String date = AppUtils.formatDateToString(new Date(AppUtils.parseLong(calendar.get(DbKeyConfig.DATE))));
                                replacementStrings.put(KeyPointConfig.DATE, date);
                                break;
                            case KeyPointConfig.INTERVIEW_TIME:
                                String interview_time = AppUtils.formatDateToString(new Date(AppUtils.parseLong(calendar.get(DbKeyConfig.INTERVIEW_TIME))));
                                replacementStrings.put(KeyPointConfig.INTERVIEW_TIME, interview_time);
                                break;
                            case KeyPointConfig.INTERVIEW_ADDRESS:
                                replacementStrings.put(KeyPointConfig.INTERVIEW_ADDRESS, AppUtils.parseString(calendar.get(DbKeyConfig.INTERVIEW_ADDRESS_NAME)));
                                break;
                            case KeyPointConfig.FLOOR:
                                replacementStrings.put(KeyPointConfig.FLOOR, AppUtils.parseString(calendar.get(DbKeyConfig.FLOOR)));
                                break;
                            case KeyPointConfig.INTERVIEW_TYPE:
                                replacementStrings.put(KeyPointConfig.INTERVIEW_TYPE, AppUtils.parseString(calendar.get(DbKeyConfig.TYPE)));
                                break;
                        }
                    }

                    //Replace keypoint
                    StrSubstitutor sub = new StrSubstitutor(replacementStrings, "{", "}");
                    if (!Strings.isNullOrEmpty(email) && AppUtils.validateEmail(email)) {
                        EmailResult emailResult = EmailResult.builder()
                                .email(email)
                                .subject(sub.replace(subject))
                                .content(sub.replace(content))
                                .build();
                        results.add(emailResult);
                    }
                }
            }
            return results;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
            return null;
        }
    }
}

