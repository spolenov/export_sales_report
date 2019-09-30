package com.century.report;

import com.century.exception.ExportSalesReportException;
import com.century.report.extra_charge.Grouping;
import com.century.report.extra_charge.Invoice;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class Util {
    private static final int REPORT_EMPTY = 4096;
    private static final String LOG_PATH = "C:\\Temp\\export_sales_report";
    private static final String EXCEL_FILE_PATH = "C:\\Temp\\export_sales_report";

    private static final String CONFIG_PATH = "C:\\Temp\\export_sales_report";
    private static final String SETTINGS_FILE_NAME = "settings.json";
    private static final String INVOICE_FILE_NAME = "invoice.json";

    private static final String LOG_FILE_NAME = "export_sales_report_%.log";
    private static final int MAX_GROUPING_COUNT = 2;
    private static final int BIGDECIMAL_SCALE = 7;

    public static String getDir(){
        File f = new File(System.getProperty("java.class.path"));
        File dir = f.getAbsoluteFile().getParentFile();
        return dir.toString();
    }

    public static int getMaxGroupingCount(){
        return MAX_GROUPING_COUNT;
    }

    public static ReportSettings parseSettings() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Reader initialReader = new InputStreamReader(
                new FileInputStream(CONFIG_PATH + "\\" + SETTINGS_FILE_NAME), UTF_8);

        String json = IOUtils.toString(initialReader);
        return objectMapper.readValue(json, ReportSettings.class);
    }

    public static List<Invoice> parseInvoices() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Reader initialReader = new InputStreamReader(
                new FileInputStream(CONFIG_PATH + "\\" + INVOICE_FILE_NAME), UTF_8);

        String json = IOUtils.toString(initialReader);
        return objectMapper.readValue(json, new TypeReference<List<Invoice>>(){});
    }

    public static void checkFileIsEmpty(File file) throws ExportSalesReportException {
        long length = file.length();
        if (length <= REPORT_EMPTY) {
            throw new ExportSalesReportException("Нет данных для отчёта по выбранным фильтрам.");
        }
    }

    public static void logToFile(String message, Exception e){
        logToFile("system", message + ":\n" +
                e.getMessage() + ":\n" +
                        Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .collect(Collectors.joining("\n----  ")));
    }

    public static void logToFile(String username, String message, Exception e){
        logToFile(username, message + ":\n" +
                e.getMessage() + ":\n" +
                Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n----  ")));
    }

    public static void logToFile(String message){
        logToFile("system", message);
    }

    private static void logWithBufferedWriter(FileWriter fw, String username, String message){
        try(BufferedWriter bw = new BufferedWriter(fw)){
            bw.write("\n" + getLine(username, message));
            bw.newLine();
        } catch (Exception e) {
            logToFile(username, message, e);
        }
    }

    private static void logWithFileWriter(String fullPath, String username, String message){
        try(FileWriter fw = new FileWriter(fullPath, true)){
            logWithBufferedWriter(fw, username, message);
        } catch (Exception e) {
            logToFile(username, message, e);
        }
    }

    public static void logToFile(String username, String message){
        try{
            String fullPath = LOG_PATH + "\\" + getLogFileName(username);
            File logFile = new File(fullPath);
            FileUtils.touch(logFile);

            logWithFileWriter(fullPath, username, message);

        } catch (Exception e){
            log.error("Failed to log to file:", e);
        }
    }

    private static String getLogFileName(String username){
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

        int month = calendar.get(Calendar.MONTH);

        String suffix = username + "_" +
                calendar.get(Calendar.YEAR) + "_" +
                (month < 10? "0" + month: month);
        return LOG_FILE_NAME.replace("%", suffix);
    }

    private static String getLine(String username, String message){
        return new SimpleDateFormat("dd.MM.yyyy HH.mm.ss")
                .format(new Date(System.currentTimeMillis())) + ": [" +
                username + "]: " + message ;
    }

    public static String getExcelFileFullPath(String fileName){
        return String.format("%s\\%s.xls",
                EXCEL_FILE_PATH, fileName);
    }

    public static int getScale(){
        return BIGDECIMAL_SCALE;
    }
}
