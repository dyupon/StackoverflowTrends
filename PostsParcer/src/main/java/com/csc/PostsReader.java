package com.csc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class PostsReader {
    private static FileInputStream inputStream = null;
    static Scanner sc = null;
    static final Map<String, String> postType = new HashMap<>(
            Map.of("posts", "PostTypeId=\"1\"", "comments", "PostTypeId=\"2\""));
    static String commonRegexp = "=\"(.*?)\"";
    static String tagRegexp = "&lt;(.*?)&gt;";

    PostsReader(String path) {
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found at: " + path);
        }
    }

    void closeIOStreams() throws IOException {
        if (inputStream != null) inputStream.close();
        if (sc != null) sc.close();
    }
}
