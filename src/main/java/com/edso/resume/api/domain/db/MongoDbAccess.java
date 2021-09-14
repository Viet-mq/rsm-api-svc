package com.edso.resume.api.domain.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Configuration
@Getter
@Setter
public class MongoDbAccess {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    @Value("${spring.data.mongodb.host}")
    private String dbHost;

    @Value("${spring.data.mongodb.port}")
    private String dbPort;

    @Value("${spring.data.mongodb.username}")
    private String dbUser;

    @Value("${spring.data.mongodb.password}")
    private String dbPass;

    @Bean("myMongoDB")
    public MongoClient getMongo() throws RuntimeException {

        int connectionsPerHost = 15;
        int maxConnectionIdleTime = 60;
        int maxConnectionLifeTime = 120;
        String encodedPwd = "";

        logger.info("Mongo DB server: {}:{}", dbHost, dbPort);
        logger.info("Mongo DB user: {}", dbUser);
        logger.info("Mongo DB pwd: {}", dbPass);
        logger.info("Mongo DB db: {}", dbName);
        logger.info("Mongo DB connectionsPerHost: {}", connectionsPerHost);
        logger.info("Mongo DB maxConnectionIdleTime: {}", maxConnectionIdleTime);
        logger.info("Mongo DB maxConnectionLifeTime: {}", maxConnectionLifeTime);

        MongoClientOptions.Builder options = MongoClientOptions.builder()
                .connectionsPerHost(connectionsPerHost)
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .maxConnectionIdleTime(maxConnectionIdleTime * 1_000)
                .maxConnectionLifeTime(maxConnectionLifeTime * 1_000);
        try {
            encodedPwd = URLEncoder.encode(dbPass, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("Exception ", ex);
        }

        String clientUrl = "mongodb://" + dbUser + ":" + encodedPwd + "@" + dbHost + ":" + dbPort + "/" + dbName;

        logger.info("db info: {}", clientUrl);

        MongoClientURI uri = new MongoClientURI(clientUrl, options);

        logger.info("Connect to MongoDB information: {}", uri);

        return new MongoClient(uri);

    }

}
