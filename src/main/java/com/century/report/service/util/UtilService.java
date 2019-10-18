package com.century.report.service.util;

import com.century.exception.ExportSalesReportException;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.UUID.randomUUID;
import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;
import static org.apache.commons.net.ftp.FTPReply.isPositiveCompletion;

@Service
public class UtilService {
    @Value("${ftp.url}")
    private String ftpUrl;

    @Value("${ftp.path}")
    private String ftpPath;

    private UtilService(){
        //NOP
    }

    public String transferToFTP(File source) throws IOException {
        FTPClient client = new FTPClient();
        client.connect(ftpUrl);

        client.login("anonymous", "anonymous");
        client.setFileType(BINARY_FILE_TYPE);
        client.setFileTransferMode(BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();

        String fileName = source.getName();

        try(InputStream inputStream = new FileInputStream(source)){
            client.storeFile(ftpPath + "/" + fileName, inputStream);
        } catch (Exception e){
            disconnectFtp(client);
            throw new ExportSalesReportException(
                    "Ошибка открытия локального файла:", e);
        }

        if (!isPositiveCompletion(client.getReplyCode())) {
            disconnectFtp(client);
            throw new ExportSalesReportException(
                    "Ошибка копирования файла на FTP: " +
                            "код результата = " + client.getReplyCode());
        }

        disconnectFtp(client);
        return "ftp://" +  ftpUrl + "/" + ftpPath + "/" + fileName;
    }

    public static String getResultFileName(){
        return randomUUID().toString();
    }

    public static String getLine(String username, String message){
        return new SimpleDateFormat("dd.MM.yyyy HH.mm.ss")
                .format(new Date(System.currentTimeMillis())) + ": [" +
                username + "]: " + message ;
    }

    private void disconnectFtp(FTPClient client){
        if (client.isConnected()) {
            try {
                client.logout();
                client.disconnect();
            } catch (IOException f) {
                // do nothing as file is already saved to server
            }
        }
    }

    private static final int REPORT_EMPTY = 4096;

    public static void checkFileIsEmpty(File file) throws ExportSalesReportException {
        long length = file.length();
        if (length <= REPORT_EMPTY) {
            throw new ExportSalesReportException("Нет данных для отчёта по выбранным фильтрам.");
        }
    }
}
