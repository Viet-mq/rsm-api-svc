package com.edso.resume.api.domain.rabbitmq.config;

import com.edso.resume.api.domain.entities.*;
import com.edso.resume.api.domain.request.CandidateRequest;
import com.edso.resume.api.domain.request.PresenterRequest;
import com.edso.resume.api.domain.request.RecruitmentCouncilRequest;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RabbitMQOnlineActions extends BaseAction {

    private final RabbitMQAccess rabbitMQAccess;
    @Value("${spring.rabbitmq.email.queue}")
    private String queueEmail;

    @Value("${spring.rabbitmq.email.exchange}")
    private String exchangeEmail;

    @Value("${spring.rabbitmq.email.routingkey}")
    private String routingKeyEmail;

    @Value("${spring.rabbitmq.profile.queue}")
    private String queueImage;

    @Value("${spring.rabbitmq.profile.exchange}")
    private String exchangeImage;

    @Value("${spring.rabbitmq.profile.routingkey}")
    private String routingKeyImage;

    public RabbitMQOnlineActions(RabbitMQAccess rabbitMQAccess) {
        this.rabbitMQAccess = rabbitMQAccess;
    }

    public void publishEmailToRabbit(String email, String time) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "fanout", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            EmailMessageEntity messageEntity = new EmailMessageEntity();
            messageEntity.setToEmail(email);
            messageEntity.setMessage("Bạn có cuộc phỏng tại công ty Edsolabs\n" +
                    "Tại: No 9, Lane 4, Duy Tân, Dịch Vọng Hậu, Cầu Giấy, Hà Nội\n" +
                    "Thời gian: " + time);
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(messageEntity).getBytes());
            channel.close();

            logger.info("=>publishEmailToRabbit toEmail: {}, time: {}", email, time);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }

    }

    public void publishCalendarCandidateEmail(String type, CandidateRequest candidate, List<String> listPath, String calendarId, String historyId, String profileId) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Email email = Email.builder()
                    .type(type)
                    .calendarId(calendarId)
                    .profileId(profileId)
                    .historyId(historyId)
                    .subject(candidate.getSubjectCandidate())
                    .content(candidate.getContentCandidate())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(email).getBytes());
            channel.close();

            logger.info("=>publishCandidateEmail email: {}", email);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishCandidateEmail(String type, CandidateRequest candidate, List<String> listPath, String historyId, String profileId) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Email email = Email.builder()
                    .type(type)
                    .profileId(profileId)
                    .historyId(historyId)
                    .subject(candidate.getSubjectCandidate())
                    .content(candidate.getContentCandidate())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(email).getBytes());
            channel.close();

            logger.info("=>publishCandidateEmail email: {}", email);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishCandidateEmails(String type, CandidateRequest candidate, List<String> listPath, List<IdEntity> ids) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Emails emails = Emails.builder()
                    .type(type)
                    .ids(ids)
                    .subject(candidate.getSubjectCandidate())
                    .content(candidate.getContentCandidate())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(emails).getBytes());
            channel.close();

            logger.info("=>publishCandidateEmail emails: {}", emails);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishRecruitmentCouncilEmail(String type, RecruitmentCouncilRequest recruitmentCouncil, List<String> listPath, String historyId, String calendarId) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            EmailRecruitmentCouncil email = EmailRecruitmentCouncil.builder()
                    .type(type)
                    .calendarId(calendarId)
                    .historyId(historyId)
                    .subject(recruitmentCouncil.getSubjectRecruitmentCouncil())
                    .content(recruitmentCouncil.getContentRecruitmentCouncil())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(email).getBytes());
            channel.close();

            logger.info("=>publishRecruitmentCouncilEmail email: {}", email);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishRecruitmentCouncilEmails(String type, RecruitmentCouncilRequest recruitmentCouncil, List<String> listPath, List<IdEntity> ids) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Emails emails = Emails.builder()
                    .type(type)
                    .ids(ids)
                    .subject(recruitmentCouncil.getSubjectRecruitmentCouncil())
                    .content(recruitmentCouncil.getContentRecruitmentCouncil())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(emails).getBytes());
            channel.close();

            logger.info("=>publishRecruitmentCouncilEmail emails: {}", emails);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishCalendarPresenterEmail(String type, PresenterRequest presenter, List<String> listPath, String calendarId, String historyId, String profileId) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Email email = Email.builder()
                    .type(type)
                    .calendarId(calendarId)
                    .profileId(profileId)
                    .historyId(historyId)
                    .subject(presenter.getSubjectPresenter())
                    .content(presenter.getContentPresenter())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(email).getBytes());
            channel.close();

            logger.info("=>publishPresenterEmail email: {}", email);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishPresenterEmail(String type, PresenterRequest presenter, List<String> listPath, String historyId, String profileId) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Email email = Email.builder()
                    .type(type)
                    .profileId(profileId)
                    .historyId(historyId)
                    .subject(presenter.getSubjectPresenter())
                    .content(presenter.getContentPresenter())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(email).getBytes());
            channel.close();

            logger.info("=>publishPresenterEmail email: {}", email);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }

    public void publishPresenterEmails(String type, PresenterRequest presenter, List<String> listPath, List<IdEntity> ids) {
        try {
            Connection connection = rabbitMQAccess.getConnection();
            Channel channel = connection.createChannel();

            //Neu khong co thi tao
            channel.exchangeDeclare(exchangeEmail, "direct", true);
            channel.queueDeclare(queueEmail, true, false, false, null);
            channel.queueBind(queueEmail, exchangeEmail, routingKeyEmail);

            Emails emails = Emails.builder()
                    .type(type)
                    .ids(ids)
                    .subject(presenter.getSubjectPresenter())
                    .content(presenter.getContentPresenter())
                    .files(listPath)
                    .build();
            BasicProperties messageProperties = new BasicProperties.Builder()
                    .contentType("application/json")
                    .build();
            channel.basicPublish(exchangeEmail, routingKeyEmail, messageProperties, new Gson().toJson(emails).getBytes());
            channel.close();

            logger.info("=>publishPresenterEmail emails: {}", emails);
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        }
    }


//    public void publishImageToRabbit(ImageEntity image) {
//        try{
//            Channel channel = rabbitMQAccess.getConnection();
//
//            //Neu khong co thi tao
//            channel.exchangeDeclare(exchangeImage, "direct", true);
//            channel.queueDeclare(queueImage, true, false, false, null);
//            channel.queueBind(queueImage, exchangeImage, routingKeyImage);
//            BasicProperties properties = new BasicProperties.Builder()
//                    .contentType("application/json")
//                    .build();
//            channel.basicPublish(exchangeImage, routingKeyImage, properties, new Gson().toJson(image).getBytes());
//            channel.close();
//
//            logger.info("=>publishImageToRabbit type: {}, image: {}", image);
//        }catch (Throwable ex){
//            logger.error("Exception: ", ex);
//        }
//
//    }

//    public void sendActionProfileToRabbit(String type, Object obj) {
//        try{
//            Channel channel = rabbitMQAccess.getConnection();
//
//            //Neu khong co thi tao
//            channel.exchangeDeclare(exchangeProfile, "direct", true);
//            channel.queueDeclare(queueProfile, true, false, false, null);
//            channel.queueBind(queueProfile, exchangeProfile, routingKeyProfile);
//
//            EventEntity eventEntity = new EventEntity(type, obj);
//            BasicProperties messageProperties = new BasicProperties.Builder()
//                    .contentType("application/json")
//                    .build();
//            channel.basicPublish(exchangeProfile, routingKeyProfile, messageProperties, new Gson().toJson(eventEntity).getBytes());
//            channel.close();
//
//            logger.info("=>sendActionProfileToRabbit type: {}, profile: {}", type, obj);
//        }catch (Throwable ex){
//            logger.error("Exception: ", ex);
//        }
//
//    }

}
