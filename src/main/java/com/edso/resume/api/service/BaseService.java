package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.edso.resume.api.domain.entities.EventEntity;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public abstract class BaseService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MongoDbOnlineSyncActions db;
    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.routingkey}")
    private String routingkey;

    protected BaseService(MongoDbOnlineSyncActions db, RabbitTemplate rabbitTemplate) {
        this.db = db;
        this.rabbitTemplate = rabbitTemplate;
    }

    protected Bson buildCondition(List<Bson> lst) {
        if (lst == null || lst.isEmpty()) {
            return new Document();
        }
        if (lst.size() == 1) {
            return lst.get(0);
        }
        return Filters.and(lst);
    }

    @SuppressWarnings(value = "unchecked")
    public List<String> parseList(Object list) {
        return (List<String>) list;
    }

    public void insertToRabbitMQ(String type, Object obj){
        EventEntity event = new EventEntity(type, obj);
        rabbitTemplate.convertAndSend(exchange, routingkey, event);
    }

}
