package com.csc;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TagsPopularityTracker extends PostsReader {

    private static String TIME_COLUMN = "CreationDate";
    private static String timeBoundsFile = "cacheTimeBounds.txt";
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final Date PAST = new Date(0);
    private static final Date FUTURE = new Date(2025, Calendar.JANUARY, 1);
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


    TagsPopularityTracker(String path) {
        super(path);
    }

    void extractInformation(String type, String period, String fileName, int tagFrequencyThreshold) {
        Date[] timeBounds = deserializeTimeBounds();
        List<String> columns = buildCols(period, timeBounds);
        createCSVFile(fileName, columns);
        Map<String, Integer> tagsFrequencies = TagsFrequenciesSerializer.deserialize();
        tagsFrequencies.values().removeIf(value -> value <= tagFrequencyThreshold);
        Set<String> tags = tagsFrequencies.keySet();
        for (String tag: tags) {

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
                }
                else cols.add(i % 4 + "." + currentYear);
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

    private Date[] deserializeTimeBounds() {
        Date[] timeBounds;
        try {
            FileInputStream fileInputStream = new FileInputStream(timeBoundsFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            timeBounds = (Date[]) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot deserialize cached time bounds: " + e.getMessage());
        }
        return timeBounds;
    }

    Date[] serializeTimeBounds(String type) {
        Date currentOldest = FUTURE;
        Date currentNewest = PAST;
        String targetType = postType.get(type);
        while (sc.hasNextLine()) {
            String dbEntry = sc.nextLine();
            if (dbEntry.contains(targetType)) {
                Pattern pattern = Pattern.compile(TIME_COLUMN + commonRegexp);
                Matcher matcher = pattern.matcher(dbEntry);
                if (matcher.find()) {
                    try {
                        Date currentTime = TIME_FORMATTER.parse(matcher.group(1));
                        if (currentTime.compareTo(currentOldest) < 0) currentOldest = currentTime;
                        if (currentTime.compareTo(currentNewest) > 0) currentNewest = currentTime;
                    } catch (ParseException e) {
                        throw new RuntimeException("Cannot parse date in line: " + dbEntry + ", " + e.getMessage());
                    }
                }
            }
        }
        Date[] result = new Date[]{currentOldest, currentNewest};
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(timeBoundsFile);
            ObjectOutput objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(result);
            fileOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize time bounds: " + e.getMessage());
        }
        return result;
    }
}



