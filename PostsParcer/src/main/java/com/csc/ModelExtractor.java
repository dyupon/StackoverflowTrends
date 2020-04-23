package com.csc;

import org.apache.commons.csv.CSVRecord;

import java.util.HashMap;
import java.util.Map;

public class ModelExtractor extends CSVReader {

    ModelExtractor(String path) {
        super(path);
    }

    Map<String, TagClustering> extractModel() {
        Map<String, TagClustering> model = new HashMap<>();
        int count = 0;
        for (CSVRecord record : csvParser) {
            ++count;
            String tag = record.get("tag");
            TagClustering tagClustering = new TagClustering(record.get("topic"), Double.parseDouble(record.get("propensity")));
            if (tagClustering.isInformative()) model.put(tag, tagClustering); // todo: somehow convert map to list
        }
        return model;
    }

    void serializeModel() {
        //todo: implement
    }

}
