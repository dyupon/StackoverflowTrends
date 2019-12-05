package com.csc;


import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;


class VWConverter extends CSVReader {
    private static BufferedWriter writer = null;

    VWConverter(String path) {
        super(path);
    }

    void convertToVW(List<String> colsToRetrieve, String fileName, String observationIdentity) {
        createTxtFile(fileName);
        int lineCount = 0;
        Instant lineCountStart = Instant.now();
        for (CSVRecord record : csvParser) {
            ++lineCount;
            if (colsToRetrieve.size() == 1) {
                String observationId = observationIdentity + record.get("Id");
                String feature = record.get(colsToRetrieve.get(0));
                try {
                    writer.write(observationId + " " + feature);
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException("Error while writing to TXT file " + fileName + " :" + e.getMessage());
                }
            } //TODO: add several feature namespaces handling (e.g. for dynamic modelling)
            if (lineCount % 100000 == 0)
                System.out.println("Elapsed: " + lineCount + " lines in " + Duration.between(lineCountStart, Instant.now()).toSeconds() + " seconds");
        }
        System.out.println("Total lines elapsed: " + lineCount + " in "
                + Duration.between(lineCountStart, Instant.now()).toMinutes() + " minutes.");
        closeIOStreams();
    }

    private void createTxtFile(String fileName) {
        File file = new File(fileName);
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException("Error while creating TXT file " + fileName);
        }
    }

    @Override
    void closeIOStreams() {
        try {
            if (csvParser != null) csvParser.close();
            super.closeIOStreams();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing IO streams: " + e.getMessage());
        }
    }
}
