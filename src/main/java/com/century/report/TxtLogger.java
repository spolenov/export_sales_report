package com.century.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.century.report.service.util.UtilService.getLine;

@Slf4j
@Component
public class TxtLogger {
    @Value("${log.path}")
    private String logPath;

    @Value("${default.username}")
    private String defaultUsername;

    @Value("${log.file.template}")
    private String logFileTemplate;

    public synchronized void logToFile(String username, String message){
        if(username == null || username.isEmpty()){
            username = defaultUsername;
        }

        try{
            String fullPath = logPath + "/" + getLogFileName(username);
            File logFile = new File(fullPath);
            FileUtils.touch(logFile);

            logWithFileWriter(fullPath, username, message);

        } catch (Exception e){
            log.error("Failed to log to file:", e);
        }
    }

    public synchronized void logToFile(String message, Exception e){
        logToFile("system", message + ":\n" +
                e.getMessage() + ":\n" +
                Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n----  ")));
    }

    public synchronized void logToFile(String username, String message, Exception e){
        logToFile(username, message + ":\n" +
                e.getMessage() + ":\n" +
                Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n----  ")));
    }

    public synchronized void logToFile(String message){
        logToFile("system", message);
    }

    private void logWithBufferedWriter(FileWriter fw, String username, String message){
        try(BufferedWriter bw = new BufferedWriter(fw)){
            bw.write("\n" + getLine(username, message));
            bw.newLine();
        } catch (Exception e) {
            logToFile(username, message, e);
        }
    }

    private void logWithFileWriter(String fullPath, String username, String message){
        try(FileWriter fw = new FileWriter(fullPath, true)){
            logWithBufferedWriter(fw, username, message);
        } catch (Exception e) {
            logToFile(username, message, e);
        }
    }

    private String getLogFileName(String username){
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

        int month = calendar.get(Calendar.MONTH);

        String suffix = username + "_" +
                calendar.get(Calendar.YEAR) + "_" +
                (month < 10? "0" + month: month);
        return logFileTemplate.replace("%", suffix);
    }
}
