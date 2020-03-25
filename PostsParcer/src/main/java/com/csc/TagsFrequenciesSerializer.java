package com.csc;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagsFrequenciesSerializer extends AbstractParcer {
    private static FileOutputStream fileOutputStream = null;
    private static ObjectOutputStream objectOutputStream = null;
    private static String fileName = "tagsFrequencies.ser";

    TagsFrequenciesSerializer(String path) {
        super(path);
    }

    void serialize() {
        createIOStreams();
        Instant lineCountStart = Instant.now();
        int linesTotal = 0;
        Map<String, Integer> tagsFrequencies = new HashMap<>();
        while (sc.hasNextLine()) {
            ++linesTotal;
            String dbEntry = sc.nextLine();
            if (dbEntry.contains(postType.get("posts"))) {
                Pattern pattern = Pattern.compile("Tags" + commonRegexp);
                Matcher matcher = pattern.matcher(dbEntry);
                if (matcher.find()) {
                    Pattern patternTag = Pattern.compile(tagRegexp);
                    Matcher matcherTag = patternTag.matcher(matcher.group(1));
                    while (matcherTag.find()) {
                        String tag = matcherTag.group(1);
                        tagsFrequencies.putIfAbsent(tag, 0);
                        tagsFrequencies.computeIfPresent(tag, (k, v) -> v + 1);
                    }
                }
            }
            if (linesTotal % 100000 == 0)
                System.out.println("Elapsed: " + linesTotal + " lines in " +
                        Duration.between(lineCountStart, Instant.now()).toSeconds() + " seconds");

        }
        try {
            objectOutputStream.writeObject(tagsFrequencies);
            closeIOStreams();
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize HashMap with tags frequencies: " + e.getMessage());
        }
        closeIOStreams();
    }

    static HashMap<String, Integer> deserialize() {
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        try {
            fileInputStream = new FileInputStream(fileName);
            objectInputStream = new ObjectInputStream(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file for de-serialization: " + e.getMessage());
        }
        HashMap<String, Integer> tagsFrequencies;
        try {
            tagsFrequencies = (HashMap<String, Integer>) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot deserialize object from file" + fileName + " " + e.getMessage());
        }
        return tagsFrequencies;
    }

    @Override
    void closeIOStreams() {
        try {
            objectOutputStream.close();
            fileOutputStream.close();
            super.closeIOStreams();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing output streams: " + e.getMessage());
        }
    }

    private void createIOStreams() {
        try {
            fileOutputStream = new FileOutputStream(fileName);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file for serialization: " +
                    e.getMessage());
        }
    }
}
