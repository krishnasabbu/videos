package com.chat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TestCaseGenerator {

    public static void main(String[] args) throws Exception {
        String jsonInput = "{\n" +
                "  \"DeliveryContent\": {\n" +
                "    \"contentType\": {\n" +
                "\t   \"type\": \"String\",\n" +
                "\t   \"mandatory\": true\n" +
                "\t},\n" +
                "\t\"alertText\": {\n" +
                "\t   \"type\": \"String\",\n" +
                "\t   \"mandatory\": true\n" +
                "\t},\n" +
                "\t\"messageTypeId\": {\n" +
                "\t   \"type\": \"String\",\n" +
                "\t   \"mandatory\": false\n" +
                "\t}\n" +
                "  },\n" +
                "  \"DeliveryMetadata\": {\n" +
                "    \"uowId\": {\n" +
                "\t   \"type\": \"String\",\n" +
                "\t   \"mandatory\": true\n" +
                "\t},\n" +
                "\t\"sorUowId\": {\n" +
                "\t   \"type\": \"String\",\n" +
                "\t   \"mandatory\": true\n" +
                "\t},\n" +
                "\t\"inboundUowId\": {\n" +
                "\t   \"type\": \"String\",\n" +
                "\t   \"mandatory\": true\n" +
                "\t}\n" +
                "  }\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonInput);

        String objectName = "DeliveryContentAudit";
        Set<String> generatedCases = new HashSet<>();
        StringBuilder sb = new StringBuilder();

        generateTestCases(rootNode, objectName, "", generatedCases, sb);

        // Print all generated test cases
        System.out.println(sb.toString());

        ObjectNode defaultJson = generateDefaultJson(rootNode);

        // Print the generated default JSON
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultJson));

        /*ObjectNode defaultJson = generateDefaultJson(rootNode);

        // Print the generated default JSON
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultJson));*/


        //generateTestCases(jsonInput);
        // Print the generated test cases
    }



    private static ObjectNode generateDefaultJson(JsonNode schemaNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode resultNode = mapper.createObjectNode();

        Iterator<Map.Entry<String, JsonNode>> fields = schemaNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            String fieldName = fieldEntry.getKey();
            JsonNode fieldProperties = fieldEntry.getValue();

            if (fieldProperties.isObject() && fieldProperties.has("type")) {
                // Handle field with type
                String type = fieldProperties.get("type").asText();
                resultNode.put(fieldName, getDefaultForType(type));
            } else if (fieldProperties.isObject()) {
                // Handle nested object
                resultNode.set(fieldName, generateDefaultJson(fieldProperties));
            }
        }

        return resultNode;
    }

    private static String getDefaultForType(String type) {
        switch (type) {
            case "String":
                return "value";
            case "Int":
            case "Integer":
                return "0";
            case "Float":
                return "0.0";
            case "Boolean":
                return "true";
            // Add more types as needed
            default:
                return "value";
        }
    }

    private static void generateTestCases(JsonNode schemaNode, String objectName, String parentPath, Set<String> generatedCases, StringBuilder sb) {
        // Generate test cases for outer objects
        Iterator<Map.Entry<String, JsonNode>> fields = schemaNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            String fieldName = fieldEntry.getKey();
            JsonNode fieldProperties = fieldEntry.getValue();

            if (fieldProperties.isObject() && !fieldProperties.has("type")) {
                // This is an outer object
                String outerObjectPath = parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName;
                generateTestCaseForMissingOuterObject(objectName, outerObjectPath, generatedCases, sb);
            } else if (fieldProperties.isObject()) {
                // Recursively process nested objects
                generateTestCases(fieldProperties, objectName, parentPath + "." + fieldName, generatedCases, sb);
            }
        }

        // Generate test cases for mandatory fields
        fields = schemaNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            String fieldName = fieldEntry.getKey();
            JsonNode fieldProperties = fieldEntry.getValue();

            if (fieldProperties.isObject() && fieldProperties.has("type") && fieldProperties.has("mandatory")) {
                boolean isMandatory = fieldProperties.get("mandatory").asBoolean();
                if (isMandatory) {
                    String fieldPath = parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName;
                    generateTestCaseForMandatoryField(objectName, fieldPath, generatedCases, sb);
                }
            } else if (fieldProperties.isObject()) {
                generateTestCases(fieldProperties, objectName, parentPath + "." + fieldName, generatedCases, sb);
            }
        }
    }

    private static void generateTestCaseForMissingOuterObject(String objectName, String fieldPath, Set<String> generatedCases, StringBuilder sb) {
        String methodName = "testRequest_when" + capitalize(fieldPath.replace(".", "")) + "IsMissing";

        if (generatedCases.contains(methodName)) {
            return; // Skip if already generated
        }
        generatedCases.add(methodName);

        String[] pathParts = fieldPath.split("\\.");
        StringBuilder setMethodChain = new StringBuilder();
        for (int i = 0; i < pathParts.length - 1; i++) {
            setMethodChain.append(pathParts[i]).append("().get");
        }
        setMethodChain.append(pathParts[pathParts.length - 1]);

        sb.append("@Test\n");
        sb.append("public void ").append(methodName).append("() {\n");
        sb.append("    ").append(objectName);
        for (int i = 0; i < pathParts.length - 1; i++) {
            if(StringUtils.isNoneEmpty(pathParts[i])) {
                sb.append(".get").append(capitalize(pathParts[i])).append("()");
            }
        }
        sb.append(".set").append(capitalize(pathParts[pathParts.length - 1])).append("(null);\n");
        sb.append("    Assertions.assertThrows(ApplicationException.class, () -> ").append(objectName).append("ServiceHelper.validateRequest(").append(objectName).append("));\n");
        sb.append("}\n\n");
    }

    private static void generateTestCaseForMandatoryField(String objectName, String fieldPath, Set<String> generatedCases, StringBuilder sb) {
        String methodName = "testRequest_when" + capitalize(fieldPath.replace(".", "")) + "IsMissing";

        if (generatedCases.contains(methodName)) {
            return; // Skip if already generated
        }
        generatedCases.add(methodName);

        String[] pathParts = fieldPath.split("\\.");
        StringBuilder setMethodChain = new StringBuilder();
        for (int i = 0; i < pathParts.length - 1; i++) {
            setMethodChain.append(pathParts[i]).append("().get");
        }
        setMethodChain.append(pathParts[pathParts.length - 1]);

        sb.append("@Test\n");
        sb.append("public void ").append(methodName).append("() {\n");
        sb.append("    ").append(objectName);
        for (int i = 0; i < pathParts.length - 1; i++) {
            if(StringUtils.isNoneEmpty(pathParts[i])) {
                sb.append(".get").append(capitalize(pathParts[i])).append("()");
            }
        }
        sb.append(".set").append(capitalize(pathParts[pathParts.length - 1])).append("(null);\n");
        sb.append("    Assertions.assertThrows(ApplicationException.class, () -> ").append(objectName).append("ServiceHelper.validateRequest(").append(objectName).append("));\n");
        sb.append("}\n\n");
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // Split the string by "."
        String[] parts = str.split("\\.");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                // Capitalize the first letter and keep the rest unchanged
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        // Remove the trailing space
        return result.toString().trim();
    }

}
