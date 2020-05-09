package com.csc;

import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelExtractor extends CSVReader {

    private HashMap<String, ArrayList<TagClustering>> repeatedTags = new HashMap<>();

    ModelExtractor(String path) {
        super(path);
    }

    HashMap<String, ArrayList<TagClustering>> getRepeatedTags() {
        return repeatedTags;
    }

    Map<String, TagClustering> extractModel() {
        Map<String, TagClustering> model = new HashMap<>();
        for (CSVRecord record : csvParser) {
            String tag = record.get("tag");
            String topic = record.get("topic");
            double propensity = Double.parseDouble(record.get("propensity"));
            TagClustering tagClustering = new TagClustering(topic, propensity);
            if (tagClustering.isInformative()) {
                TagClustering previous = model.put(tag, tagClustering);
                if (previous != null) {
                    ArrayList<TagClustering> existing = repeatedTags.get(tag);
                    if (existing == null) {
                        repeatedTags.put(tag, new ArrayList<>(Collections.singletonList(new TagClustering(previous.getTopic(), previous.getPropensity(), tag))));
                    } else {
                        existing.add(new TagClustering(previous.getTopic(), previous.getPropensity(), tag));
                        repeatedTags.put(tag, existing);
                    }
                }
            }
        }
        return model;
    }

    void serializeModel() {
        //todo: implement
    }

}
