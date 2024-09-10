package com.chat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

@Component
public class GenerateFolder {

    private static Set<String> seenPaths = new HashSet<>();

    public static void main(String[] args) {
        // Specify the folder path here
        String folderPath = "D:\\ChatGPT\\ChatService";

        // Create the root folder object

        Path rootPath = Paths.get(folderPath);

        try {
            JSONObject json = createRootJson();
            addDirectory(json, rootPath);
            String jsonString = json.toString(2); // Pretty print with indentation
            String formattedJsonString = formatJsonKeys(jsonString);
            System.out.println(formattedJsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public String getFolderPath(String path) {
        File rootFolder = new File(path);
        // Generate JSON structure
        ObjectNode folderJson = generateFolderJson(rootFolder);

        // Convert JSON to string
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Object> list = Arrays.asList(folderJson);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }*/

    public String getFolderPath(String path) {
        try {
            Path rootPath = Paths.get(path);
            JSONObject json = createRootJson();
            addDirectory(json, rootPath);
            String jsonString = json.toString(2); // Pretty print with indentation
            String formattedJsonString = formatJsonKeys(jsonString);
            System.out.println(jsonString);
            return jsonString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readContent(String path, String fileName) {
        try {
            Optional<Path> filePath = findFile(Paths.get(path), fileName);
            if (filePath.isPresent()) {
                String content = readFileContent(filePath.get());
                System.out.println("File content:");
                System.out.println(content);
                return content;
            } else {
                System.out.println("File not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Optional<Path> findFile(Path directory, String fileName) throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst();
        }
    }

    // Read the content of the file
    public static String readFileContent(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private static ObjectNode generateFolderJson(File folder) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode folderNode = mapper.createObjectNode();
        folderNode.put("name", folder.getName());

        if (folder.isDirectory()) {
            ArrayNode childrenArray = mapper.createArrayNode();

            File[] children = folder.listFiles();
            if (children != null) {
                for (File child : children) {
                    childrenArray.add(generateFolderJson(child));
                }
            }
            folderNode.set("children", childrenArray);
        }

        return folderNode;
    }

    private static JSONObject createRootJson() {
        JSONObject rootJson = new JSONObject();
        rootJson.put("name", "ChatService");
        rootJson.put("children", new JSONArray());
        return rootJson;
    }

    private static void addDirectory(JSONObject parentJson, Path dir) throws IOException {
        seenPaths = new HashSet<>();
        if (Files.notExists(dir)) {
            return;
        }

        if (seenPaths.contains(dir.toAbsolutePath().toString())) {
            return;
        }
        seenPaths.add(dir.toAbsolutePath().toString());

        JSONArray children = parentJson.getJSONArray("children");

        JSONObject dirJson = new JSONObject();
        dirJson.put("name", dir.getFileName().toString());
        dirJson.put("children", new JSONArray());
        children.put(dirJson);

        // Recursively add subdirectories and files
        File[] subDirs = dir.toFile().listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                addDirectory(dirJson, subDir.toPath());
            }
        }

        File[] files = dir.toFile().listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                addFile(dirJson, file.toPath());
            }
        }
    }

    private static void addFile(JSONObject parentJson, Path file) {
        if (Files.notExists(file)) {
            return;
        }

        if (seenPaths.contains(file.toAbsolutePath().toString())) {
            return;
        }
        seenPaths.add(file.toAbsolutePath().toString());

        JSONArray children = parentJson.getJSONArray("children");
        JSONObject fileJson = new JSONObject();
        fileJson.put("name", file.getFileName().toString());
        fileJson.put("content", "This is text");
        children.put(fileJson);
    }

    private static String formatJsonKeys(String jsonString) {
        // Remove quotes around keys by replacing patterns in the JSON string
        return jsonString
                .replaceAll("\"([^\"]+)\":", "$1:") // Remove quotes around keys
                .replaceAll(",\\s*}", "}"); // Remove trailing comma before closing brace
    }


}
