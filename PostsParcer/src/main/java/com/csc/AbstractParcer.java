package com.csc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class AbstractParcer {
    private static FileInputStream inputStream = null;
    static CSVPrinter csvPrinter = null;
    static FileWriter fileWriter = null;
    static Scanner sc = null;
    static final Map<String, String> postType = new HashMap<>(
            Map.of("posts", "PostTypeId=\"1\"", "comments", "PostTypeId=\"2\""));
    static String commonRegexp = "=\"(.*?)\"";
    static String tagRegexp = "&lt;(.*?)&gt;";

    AbstractParcer(String path) {
        if (!path.isEmpty()) try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found at: " + path);
        }
    }

    void createCSVFile(String fileName, List<String> headers) {
        try {
            fileWriter = new FileWriter(fileName);
            String[] headersArr = headers.toArray(new String[0]);
            csvPrinter = new CSVPrinter(fileWriter,
                    CSVFormat.DEFAULT.withHeader(headersArr));
        } catch (Exception e) {
            throw new RuntimeException("Error while creating output file: " + e.getMessage());
        }
    }

    void closeIOStreams() throws IOException {
        if (inputStream != null) inputStream.close();
        if (sc != null) sc.close();
        if (fileWriter != null) fileWriter.close();
        if (csvPrinter != null) csvPrinter.close();
    }
}
