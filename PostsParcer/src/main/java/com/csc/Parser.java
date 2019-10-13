package com.csc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@SuppressWarnings("SameParameterValue")
class Parser {
    private static String path;
    private static FileInputStream inputStream = null;
    private static Scanner sc = null;
    private static final Map<String, String> postType = new HashMap<>(
            Map.of("posts", "PostTypeId=\"1\"", "comments", "PostTypeId=\"2\""));
    private static CSVPrinter csvPrinter = null;
    private static FileWriter fileWriter = null;
    private static String commonRegexp = "=\"(.*?)\"";
    private static String tagRegexp = "&lt;(.*?)&gt;";

    Parser(String path) {
        Parser.path = path;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found at: " + path);
        }
    }

    void parseToCSV(String type, List<String> colsToRetrieve, String fileName) {
        String targetType = postType.get(type);
        int linesTotal = 0;
        int linesTarget = 0;
        createCSVFile(fileName, colsToRetrieve);
        Instant lineCountStart = Instant.now();
        while (sc.hasNextLine()) {
            ++linesTotal;
            String dbEntry = sc.nextLine();
            if (dbEntry.contains(targetType)) {
                ++linesTarget;
                List<String> row = new ArrayList<>();
                List<String> tags = new ArrayList<>();
                IntStream.range(0, colsToRetrieve.size()).forEach(i -> {
                    String processedCol = colsToRetrieve.get(i);
                    Pattern pattern = Pattern.compile(processedCol + commonRegexp);
                    Matcher matcher = pattern.matcher(dbEntry);
                    if (processedCol.equals("Tags") && matcher.find()) {
                        Pattern patternTag = Pattern.compile(tagRegexp);
                        Matcher matcherTag = patternTag.matcher(matcher.group(1));
                        while (matcherTag.find()) {
                            tags.add(matcherTag.group(1));
                        }
                    } else if (matcher.find()) {
                        row.add(matcher.group(1));
                    } else {
                        row.add("NaN");
                    }
                });
                try {
                    for (String tag : tags) {
                        row.add(tag);
                        csvPrinter.printRecord(row);
                        csvPrinter.flush();
                        row.remove(row.size() - 1);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Cannot write line " + row.toString() + " created from " + dbEntry + " to CSV");
                }
            }
            if (linesTotal % 100000 == 0)
                System.out.println("Elapsed: " + linesTotal + " lines in " + Duration.between(lineCountStart, Instant.now()).toSeconds() + " seconds");
        }
        System.out.println("Total lines elapsed: " + linesTotal + ", target lines elapsed: " + linesTarget + " in "
                + Duration.between(lineCountStart, Instant.now()).toMinutes() + " minutes");
    }

    private void createCSVFile(String fileName, List<String> headers) {
        try {
            fileWriter = new FileWriter(fileName);
            String[] headersArr = headers.toArray(new String[0]);
            csvPrinter = new CSVPrinter(fileWriter,
                    CSVFormat.DEFAULT.withHeader(headersArr));
            csvPrinter.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error while creating output file: " + e.getMessage());
        }
    }

    void flush() {
        try {
            if (fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
            }
            if (inputStream != null) inputStream.close();
            if (csvPrinter != null) csvPrinter.close();
        } catch (IOException ex) {
            throw new RuntimeException("Error while closing output streams: " + ex.getMessage());
        } finally {
            if (sc != null) sc.close();
        }
    }

    void printLine(String type, int amount) {
        String targetType = postType.get(type);
        for (int i = 0; i < amount; ++i) {
            if (sc.hasNextLine()) {
                String dbEntry = sc.nextLine();
                if (dbEntry.contains(targetType)) System.out.println(dbEntry);
            }
        }
    }
}
