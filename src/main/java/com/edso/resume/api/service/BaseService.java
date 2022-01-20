package com.edso.resume.api.service;

import com.edso.resume.api.domain.db.MongoDbOnlineSyncActions;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class BaseService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final MongoDbOnlineSyncActions db;

    protected BaseService(MongoDbOnlineSyncActions db) {
        this.db = db;
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

    public String parseDate(Long time) {
        Date dateTime = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        return format.format(dateTime);
    }

    public String parseDateMonthYear(Long time) {
        Date dateTime = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(dateTime);
    }

    public String randomColor() {
        Random random = new Random();
        return String.format("#%06x", random.nextInt(256 * 256 * 256));
    }

    public String saveFile(String serverPath, MultipartFile file) {
        FileOutputStream fos = null;
        try {
            String fileName = file.getOriginalFilename();
            File file1 = new File(serverPath + fileName);
            int i = 0;
            while (file1.exists()) {
                i++;
                String[] arr = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
                fileName = arr[0] + " (" + i + ")." + arr[1];
                file1 = new File(serverPath + fileName);
            }
            fos = new FileOutputStream(file1);
            fos.write(file.getBytes());
            return fileName;
        } catch (Throwable ex) {
            logger.error("Exception: ", ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable ex) {
                    logger.error("Exception: ", ex);
                }
            }
        }
        return null;
    }

    public void deleteFile(String path) {
        File file = new File(path);
        if (file.delete()) {
            logger.info("deleteFile filePath:{}", path);
        }
    }
}
