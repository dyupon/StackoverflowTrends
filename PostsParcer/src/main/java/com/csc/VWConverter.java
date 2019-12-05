package com.csc;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;


class VWConverter {
    private static CSVParser csvParser = null;
    private static BufferedWriter writer = null;

    VWConverter(String path) {
        try {
            Reader reader = new FileReader(path);
            csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException("Cannot process CSV file at: " + path + " " + e.getMessage());
        }
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

    private void closeIOStreams() {
        try {
            if (csvParser != null) csvParser.close();
            if (writer != null) writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing IO streams: " + e.getMessage());
        }
    }
}
