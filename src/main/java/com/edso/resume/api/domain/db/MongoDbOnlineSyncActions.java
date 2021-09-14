package com.edso.resume.api.domain.db;

import com.mongodb.MongoClient;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoDbOnlineSyncActions extends BaseMongoAction {

    private final MongoDbAccess mongoDbAccess;
    @Value("${spring.data.mongodb.database}")
    private String dbName;

    public MongoDbOnlineSyncActions(MongoDbAccess mongoDbAccess) {
        this.mongoDbAccess = mongoDbAccess;
    }

    public long delete(String collection, Bson condition) {
        long ret = 0L;
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            if (condition == null) {
                condition = new Document();
            }
            DeleteResult result = database.getCollection(collection).deleteMany(condition);
            ret = result.getDeletedCount();
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
        }
        return ret;
    }

    public List<Document> findAll(String collection, Bson params, Bson sort, int start, int limit) {
        List<Document> result = new ArrayList<>();
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            if (params == null) {
                params = new Document();
            }
            if (sort == null) {
                sort = new Document();
            }
            FindIterable<Document> iterable = database.getCollection(collection).find(params).sort(sort).skip(start).limit(limit);
            result = new ArrayList<>();
            for (Document document : iterable) {
                document.remove("_id");
                result.add(document);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
        }
        return result;
    }

    public FindIterable<Document> findAll2(String collection, Bson params, Bson sort, int start, int limit) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            if (params == null) {
                params = new Document();
            }
            if (sort == null) {
                sort = new Document();
            }
            return database.getCollection(collection).find(params).sort(sort).skip(start).limit(limit);
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
        }
        return null;
    }

    public long countAll(String collection, Bson params) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            if (params == null) {
                params = new Document();
            }
            return database.getCollection(collection).countDocuments(params);
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
        }
        return 0;
    }

    public Document findOne(String collectionName, Bson params) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            return database.getCollection(collectionName).find(params).first();
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
        }
        return null;
    }

    public long update(String collectionName, Bson condition, Bson values) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            UpdateResult result = database.getCollection(collectionName).updateMany(condition, values);
            logger.info("update: {} result: {}", collectionName, result);
            return result.getModifiedCount();
        } catch (Exception ex) {
            logger.error("Ex: ", ex);
        }
        return 0;
    }

    public long update(String collectionName, Bson condition, Bson values, boolean upsert) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            UpdateOptions options = new UpdateOptions().upsert(upsert);
            UpdateResult result = database.getCollection(collectionName).updateMany(condition, values, options);
            logger.info("update: {} result: {}", collectionName, result);
            return result.getModifiedCount();
        } catch (Exception ex) {
            logger.error("Ex: ", ex);
        }
        return 0;
    }

    public void bulkUpdate(String collectionName, List<WriteModel<Document>> data) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            BulkWriteOptions bulkWriteOptions = new BulkWriteOptions();
            bulkWriteOptions.ordered(false);
            BulkWriteResult result = database.getCollection(collectionName).bulkWrite(data, bulkWriteOptions);
            logger.info("update: {} result: {}", collectionName, result);
        } catch (Exception ex) {
            logger.error("Ex: ", ex);
        }
    }

    public void insertOne(String collectionName, Document document) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            database.getCollection(collectionName).insertOne(document);
        } catch (Exception ex) {
            logger.error("Ex: ", ex);
        }
    }

    public void insertMany(String collectionName, List<Document> documents) {
        try {
            MongoClient mongoClient = mongoDbAccess.getMongo();
            MongoDatabase database = mongoClient.getDatabase(dbName);
            database.getCollection(collectionName).insertMany(documents);
        } catch (Exception ex) {
            logger.error("Ex: ", ex);
        }
    }

}
