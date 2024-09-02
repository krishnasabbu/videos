package com.chat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GenerateFolder {

    public static void main(String[] args) {
        // Specify the folder path here
        String folderPath = "D:\\ChatGPT\\ChatService";

        // Create the root folder object

    }

    public String getFolderPath(String path) {
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
}
