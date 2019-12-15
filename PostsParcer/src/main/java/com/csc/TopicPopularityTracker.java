package com.csc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TopicPopularityTracker extends CSVReader {

    private static List<String> columns = new ArrayList<>();
    private static List<LocalDateTime> periods = new ArrayList<>();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static CSVPrinter csvPrinter = null;
    private static FileWriter fileWriter = null;

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

    private static Map<Integer, Integer> QUARTERS_ENDS = new HashMap<>() {{
        put(1, 3); // Q1: 1 January – 31 March
        put(2, 6); // Q2: 1 April – 30 June
        put(3, 9); // Q3: 1 July – 30 September
        put(4, 12); // Q4: 1 October – 31 December
    }};

    private static TimeBoundsSerializer serializer;

    public TopicPopularityTracker(String fileName) {
        super(fileName);
        serializer = new TimeBoundsSerializer(fileName);
    }

    void extractInformation(String fileName, String period, int tagFrequencyThreshold) {
        Date[] timeBoundsDate = serializer.deserializeTimeBounds(); //todo: initially serialize to LocalDateTime
        LocalDateTime[] timeBounds = new LocalDateTime[]{
                timeBoundsDate[0].toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                timeBoundsDate[1].toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        };
        columns.add("Tag");
        buildHeaders(period, timeBounds);
        columns.add("TopicNumber");
        columns.add("Propensity");
        Map<String, Integer> notCountedTags = TagsFrequenciesSerializer.deserialize();
        notCountedTags.values().removeIf(value -> value > tagFrequencyThreshold);
        ModelExtractor modelExtractor = new ModelExtractor("tag2topic_final1.csv"); //todo: set model from above
        Map<String, TagClustering> model = modelExtractor.extractModel();
        model.keySet().removeAll(notCountedTags.keySet());
        Map<String, List<String>> rows = new HashMap<>();
        for (String tag : model.keySet()) {
            List<String> row = new ArrayList<>(Collections.nCopies(columns.size(), String.valueOf(0)));
            Collections.fill(row, String.valueOf(0));
            row.set(columns.size() - 1, String.valueOf(model.get(tag).getPropensity()));
            row.set(columns.size() - 2, model.get(tag).getTopic());
            rows.put(tag, row);
        }
        int lineCount = 0;
        Instant lineCountStart = Instant.now();
        for (CSVRecord record : csvParser) {
            ++lineCount;
            String date = record.get("CreationDate");
            String tag = record.get("Tags");
            List<String> currentEntry = rows.get(tag);
            if (currentEntry == null) continue;
            currentEntry.set(0, tag);
            int colNum = getColToUpdate(date);
            int currentFreq = Integer.parseInt(currentEntry.get(colNum));
            currentEntry.set(colNum, String.valueOf(currentFreq + 1));
            rows.put(tag, currentEntry);
            if (lineCount % 1000000 == 0) {
                System.out.println("Elapsed: " + lineCount + " lines in " + Duration.between(lineCountStart, Instant.now()).toSeconds() + " seconds");
            }
        }
        createCSVFile(fileName, columns);
        for (Map.Entry<String, List<String>> entry : rows.entrySet()) {
            try {
                List<String> row = entry.getValue();
                csvPrinter.printRecord(row);
                csvPrinter.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error while writing row " + entry.getValue() + ": " + e.getMessage());
            }
        }
    }

    private int getColToUpdate(String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date, TIME_FORMATTER);
        //todo: binsearch over periods
        for (int i = 1; i < periods.size(); ++i) {
            if ((periods.get(i - 1).compareTo(dateTime) <= 0) && (periods.get(i).compareTo(dateTime) > 0)) {
                return i;
            }
        }
        throw new RuntimeException("Some tags appeared exactly at periods joining: " + dateTime.toString());
    }

    private void buildHeaders(String periodType, LocalDateTime[] timeBounds) {
        periods.add(timeBounds[0]);
        int currentYear = timeBounds[0].getYear();
        int shift = timeBounds[1].getYear() - timeBounds[0].getYear();
        if (periodType.equals("quarter")) {
            int lowerQuarter = QUARTERS.get(timeBounds[0].getMonthValue());
            int upperQuarter = QUARTERS.get(timeBounds[1].getMonthValue());
            int numCols = (shift - 1) * 4 + (4 - lowerQuarter + 1) + upperQuarter;
            for (int i = lowerQuarter; i <= lowerQuarter + numCols; ++i) {
                if (i % 4 == 0) {
                    columns.add("Q4." + currentYear);
                    periods.add(getEndOfPeriod(periodType, 4, currentYear));
                    ++currentYear;
                } else {
                    columns.add("Q" + i % 4 + "." + currentYear);
                    periods.add(getEndOfPeriod(periodType, i % 4, currentYear));
                }
            }
        } else if (periodType.equals("half-year")) {
            int lowerYear = timeBounds[0].getYear();
            int upperYear = timeBounds[1].getYear();
            boolean isSecondHalf = (timeBounds[0].getMonthValue() > 6);
            for (int i = lowerYear; i <= upperYear; ++i) {
                if (isSecondHalf) {
                    columns.add("H2." + i);
                    periods.add(getEndOfPeriod(periodType, 2, currentYear));
                    ++currentYear;
                } else {
                    columns.add("H1." + i);
                    columns.add("H2." + i);
                    periods.add(getEndOfPeriod(periodType, 1, currentYear));
                    periods.add(getEndOfPeriod(periodType, 2, currentYear));
                    ++currentYear;
                }
                isSecondHalf = false;
            }
        } else throw new IllegalArgumentException("Splitting on " + periodType + " is not supported");
        periods.add(timeBounds[1]);
    }

    private LocalDateTime getEndOfPeriod(String periodType, int period, int year) {
        LocalDateTime result = null;
        if (periodType.equals("quarter")) {
            if (period == 1 || period == 4) result = LocalDateTime.of(year, QUARTERS_ENDS.get(period),
                    31, 23, 59, 59, 999);
            if (period == 2 || period == 3) result = LocalDateTime.of(year, QUARTERS_ENDS.get(period),
                    30, 23, 59, 59, 999);
        } else if (periodType.equals("half-year")) {
            if (period == 1) result = LocalDateTime.of(year, 6,
                    30, 23, 59, 59, 999);
            if (period == 2) result = LocalDateTime.of(year, 12,
                    31, 23, 59, 59, 999);
        } else throw new IllegalArgumentException("Splitting on " + periodType + " is not supported");
        return result;
    }

    private void createCSVFile(String fileName, List<String> headers) {
        try {
            fileWriter = new FileWriter(fileName);
            String[] headersArr = headers.toArray(new String[0]);
            csvPrinter = new CSVPrinter(fileWriter,
                    CSVFormat.DEFAULT.withHeader(headersArr));
        } catch (Exception e) {
            throw new RuntimeException("Error while creating output file: " + e.getMessage());
        }
    }

    @Override
    void closeIOStreams() throws IOException {
        if (csvPrinter != null) csvPrinter.close();
        if (fileWriter != null) fileWriter.close();
        super.closeIOStreams();
    }
}
