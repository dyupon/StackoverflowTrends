package com.csc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PostsReader {
    protected static FileInputStream inputStream = null;
    protected static Scanner sc = null;
    protected static final Map<String, String> postType = new HashMap<>(
            Map.of("posts", "PostTypeId=\"1\"", "comments", "PostTypeId=\"2\""));
    protected static String commonRegexp = "=\"(.*?)\"";
    protected static String tagRegexp = "&lt;(.*?)&gt;";

    PostsReader(String path) {
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found at: " + path);
        }
    }

    public PostsReader() {
    }

    void closeIOStreams() throws IOException {
        if (inputStream != null) inputStream.close();
        if (sc != null) sc.close();
    }
}
