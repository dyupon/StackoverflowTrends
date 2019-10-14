package com.csc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //parseRawData();
        parseToVW();
    }

    private static void parseToVW() {
        VWConverter vwConverter = new VWConverter("DynamicPostTagsUngathered.csv");
        List<String> colsToRetrieve = new ArrayList<>() {{
           add("Tags");
        }};
        vwConverter.convertToVW(colsToRetrieve, "vw.tags.txt", "post");
        vwConverter.flush();
    }

    private static void parseRawData() {
        Parser parser = new Parser("Posts.xml");
        String entryType = "posts";
        List<String> cols = new ArrayList<>() {{
            add("Id");
            add("CreationDate");
            add("Score");
            add("OwnerUserId");
            add("Tags");
        }};
        parser.parseToCSV(entryType, cols, "DynamicPostTagsUngathered.csv");
        parser.printLine(entryType, 10);
        parser.flush();
    }
}

