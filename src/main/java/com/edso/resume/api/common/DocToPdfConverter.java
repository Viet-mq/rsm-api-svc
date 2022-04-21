package com.edso.resume.api.common;

import com.pdftron.pdf.Convert;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFNet;
import com.pdftron.sdf.SDFDoc;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

public class DocToPdfConverter {

    @SneakyThrows
    public static String convertWordToPdf(String serverPath, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        File file1 = new File(serverPath + fileName);
        int i = 0;
        String[] arr = fileName.split("\\.");
        while (file1.exists()) {
            i++;
            file1 = new File(serverPath + arr[0] + " (" + i + ")." + arr[1]);
        }
        FileOutputStream fos = new FileOutputStream(file1);
        fos.write(file.getBytes());
        fos.close();

        fileName = arr[0] + ".pdf";
        File file2 = new File(serverPath + fileName);
        int i1 = 0;
        while (file2.exists()) {
            i1++;
            file2 = new File(serverPath + arr[0] + " (" + i1 + ").pdf");
        }

        PDFNet.initialize("demo:1645258192634:7b1ffd6d030000000075edece7012015e52a10fd554abf8173305df80e");
        PDFDoc pdfdoc = new PDFDoc();
        Convert.wordToPdf(pdfdoc, file1.getPath(), null);
        pdfdoc.save(file2.getPath(), SDFDoc.e_remove_unused, null);

        return file2.getName();
    }

}
