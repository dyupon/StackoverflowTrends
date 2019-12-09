package com.csc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CSVReader {

    CSVParser csvParser;

    CSVReader(String path) {
        try {
            Reader reader = new FileReader(path);
            csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException("Cannot process CSV file at: " + path + " " + e.getMessage());
        }
    }

    void closeIOStreams() throws IOException {
        if (csvParser != null) csvParser.close();
    }
}
