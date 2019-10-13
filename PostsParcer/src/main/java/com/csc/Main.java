package com.csc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser("Posts.xml");
        String entryType = "posts";
        List<String> cols = new ArrayList<>() {{
            add("Id");
            add("CreationDate");
            add("Score");
            add("OwnerUserId");
            add("Tags");
        }};
        parser.parseToCSV(entryType, cols, "DynamicPostTags.csv");
        //parser.printLine(entryType, 10);
        parser.flush();
    }
}

