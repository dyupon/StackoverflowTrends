package com.csc;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@SuppressWarnings("SameParameterValue")
class Parser extends AbstractParcer {

    private int tagFrequencyThreshold;

    Parser(String path) {
        super(path);
    }

    void setTagFrequencyThreshold(int tagFrequencyThreshold) {
        this.tagFrequencyThreshold = tagFrequencyThreshold;
    }

    void parseToCSV(String type, List<String> colsToRetrieve, String fileName) {
        String targetType = postType.get(type);
        int linesTotal = 0;
        int linesTarget = 0;
        int linesInvalid = 0;
        createCSVFile(fileName, colsToRetrieve);
        Set<String> tagsToRetrieve = cutOffTags();
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
                            String tag = matcherTag.group(1);
                            if (tagsToRetrieve.contains(tag)) tags.add(tag);
                        }
                    } else if (matcher.find()) {
                        row.add(matcher.group(1));
                    } else {
                        row.add("NaN");
                    }
                });
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String tag : tags) {
                        sb.append(tag);
                        sb.append(" ");
                    }
                    int len = sb.length();
                    if (len > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        row.add(sb.toString());
                        csvPrinter.printRecord(row);
                        csvPrinter.flush();
                    } else ++linesInvalid;
                } catch (IOException e) {
                    throw new RuntimeException("Cannot write line " + row.toString() + " created from " + dbEntry + " to CSV");
                }
            }
            if (linesTotal % 100000 == 0)
                System.out.println("Elapsed: " + linesTotal + " lines in " + Duration.between(lineCountStart, Instant.now()).toSeconds() + " seconds");
        }
        System.out.println("Total lines elapsed: " + linesTotal + ", target lines elapsed: " + linesTarget +
                ", lines without tags elapsed: " + linesInvalid + " in "
                + Duration.between(lineCountStart, Instant.now()).toMinutes() + " minutes.");
        try {
            super.closeIOStreams();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing output streams: " + e.getMessage());
        }
    }

    private Set<String> cutOffTags() {
        Map<String, Integer> tagsFrequencies = TagsFrequenciesSerializer.deserialize();
        System.out.println("Amount of tags contained in database: " + tagsFrequencies.size());
        tagsFrequencies.values().removeIf(value -> value <= tagFrequencyThreshold);
        System.out.println("Amount of tags after cutting off the threshold: " + tagsFrequencies.size());
        return tagsFrequencies.keySet();
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
