package com.csc;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TimeBoundsSerializer extends PostsReader {

    private static String TIME_COLUMN = "CreationDate";
    private static String timeBoundsFile = "cacheTimeBounds.txt";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final Date PAST = new Date(0);
    private static final Date FUTURE = new Date(2025, Calendar.JANUARY, 1);

    TimeBoundsSerializer(String path) {
        super(path);
    }

    Date[] deserializeTimeBounds() {
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
                        long millis = TIME_FORMATTER.parseMillis(matcher.group(1));
                        Date currentTime = new Date(millis);
                        if (currentTime.compareTo(currentOldest) < 0) currentOldest = currentTime;
                        if (currentTime.compareTo(currentNewest) > 0) currentNewest = currentTime;
                    } catch (Exception e) {
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





