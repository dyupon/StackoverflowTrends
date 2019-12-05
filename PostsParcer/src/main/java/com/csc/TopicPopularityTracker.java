package com.csc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.util.*;

public class TopicPopularityTracker extends CSVReader {

    private static FileWriter fileWriter = null;
    private static CSVPrinter csvPrinter = null;
    private static String TIME_COLUMN = "CreationDate";
    private static String TAG_COLUMN = "CreationDate";
    private static String TOPIC_COLUMN = "TopicNumber";



    private static Map<Integer, Integer> QUARTERS = new HashMap<>() {{
        put(Calendar.JANUARY, 1);
        put(Calendar.FEBRUARY, 1);
        put(Calendar.MARCH, 1);
        put(Calendar.APRIL, 2);
        put(Calendar.MAY, 2);
        put(Calendar.JUNE, 2);
        put(Calendar.JULY, 3);
        put(Calendar.AUGUST, 3);
        put(Calendar.SEPTEMBER, 3);
        put(Calendar.OCTOBER, 4);
        put(Calendar.NOVEMBER, 4);
        put(Calendar.DECEMBER, 4);
    }};

    private static TimeBoundsSerializer serializer;

    public TopicPopularityTracker(String fileName) {
        super(fileName);
        serializer = new TimeBoundsSerializer(fileName);
    }

    void extractInformation(String type, String period, String fileName, int tagFrequencyThreshold) {
        Date[] timeBounds = serializer.deserializeTimeBounds();
        List<String> columns = buildCols(period, timeBounds);
        columns.add(TOPIC_COLUMN);
        createCSVFile(fileName, columns);
        Map<String, Integer> tagsFrequencies = TagsFrequenciesSerializer.deserialize();
        tagsFrequencies.values().removeIf(value -> value <= tagFrequencyThreshold);
        Set<String> tags = tagsFrequencies.keySet();
        int lineCount = 0;
        for (CSVRecord record : csvParser) {
            String date = record.get(TIME_COLUMN);
            String tag = record.get(TAG_COLUMN);
        }
        //todo: retrieve mapping <Tag, TopicNumber> from Python side
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

    private List<String> buildCols(String period, Date[] timeBounds) {
        List<String> cols = new ArrayList<>();
        int currentYear = timeBounds[0].getYear() + 1900;
        int shift = timeBounds[1].getYear() - timeBounds[0].getYear();
        if (period.equals("quarter")) {
            int lowerQuarter = QUARTERS.get(timeBounds[0].getMonth());
            int upperQuarter = QUARTERS.get(timeBounds[1].getMonth());
            int numCols = (shift - 1) * 4 + (4 - lowerQuarter + 1) + upperQuarter;
            for (int i = lowerQuarter; i <= lowerQuarter + numCols; ++i) {
                if (i % 4 == 0) {
                    cols.add("4." + currentYear);
                    ++currentYear;
                } else cols.add(i % 4 + "." + currentYear);
            }
        }

        if (period.equals("half-year")) {
            int lowerYear = timeBounds[0].getYear() + 1900;
            int upperYear = timeBounds[1].getYear() + 1900;
            boolean isSecondHalf = (timeBounds[0].getMonth() > 6);
            for (int i = lowerYear; i <= upperYear; ++i) {
                if (isSecondHalf)
                    cols.add("2." + i);
                else {
                    cols.add("1." + i);
                    cols.add("2." + i);
                }
                isSecondHalf = false;
            }
        }
        return cols;
    }
}
