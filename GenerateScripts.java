package com.chat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class GenerateScripts {

    public static void main(String[] args) throws Exception {
        GenerateScripts generateScripts = new GenerateScripts();

        String inputJSON = "{\n" +
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

        generateScripts.generateValidationScript(inputJSON);
    }

    public static void generateValidationScript(String jsonInput) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonInput);

        StringBuilder script = new StringBuilder();

        // Handle outer object null checks
        List<String> outerObjectChecks = new ArrayList<>();
        List<String> outerObjectNames = new ArrayList<>();

        rootNode.fieldNames().forEachRemaining(outerField -> {
            outerObjectNames.add(capitalize(outerField));
            outerObjectChecks.add("deliveryContentAudit.get" + capitalize(outerField) + "()");
        });

        if (!outerObjectChecks.isEmpty()) {
            script.append("if (ObjectUtils.isAnyNull(")
                    .append(String.join(", ", outerObjectChecks))
                    .append(")) {\n")
                    .append("    throw new ApplicationException(\"")
                    .append(String.join(" / ", outerObjectNames))
                    .append(" is required but missing\");\n")
                    .append("}\n\n");
        }

        // Handle nested mandatory fields
        List<String> stringMandatoryFields = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        List<String> lastFieldNames = new ArrayList<>();

        collectMandatoryStringFields(rootNode, stringMandatoryFields, fieldNames, "", lastFieldNames);

        if (!stringMandatoryFields.isEmpty()) {
            script.append("if (StringUtils.isAnyBlank(")
                    .append(String.join(", ", stringMandatoryFields))
                    .append(")) {\n")
                    .append("    throw new ApplicationException(\"")
                    .append(String.join(" / ", fieldNames))
                    .append(" is required but missing\");\n")
                    .append("}\n\n");
        }

        // Print the generated script
        System.out.println(script.toString());
    }

    private static void collectMandatoryStringFields(JsonNode node, List<String> stringFields, List<String> fieldNames, String parentPath, List<String> lastFieldName) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {

            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            String fieldName = fieldEntry.getKey();
            JsonNode fieldProperties = fieldEntry.getValue();

            if (fieldProperties.isObject()) {
                lastFieldName.clear();
                lastFieldName.add(capitalize(fieldName));
                // If the property is an object, recurse into it
                collectMandatoryStringFields(fieldProperties, stringFields, fieldNames, appendPath(parentPath, fieldName), lastFieldName);

            } else {
                // If the property is a value, check if it's a String and mandatory
                String type = fieldProperties.asText(); // This is a value, not an object
                boolean isMandatory = false;
                if (node.has(fieldName)) {
                    JsonNode field = node.get(fieldName);
                    isMandatory = field.asBoolean();
                }
                if (isMandatory) {
                    // Collect String fields for validation
                    stringFields.add(parentPath);
                    fieldNames.addAll(lastFieldName);
                }
            }
        }
    }

    private static boolean isStringType(String type) {
        return "String".equalsIgnoreCase(type);
    }

    private static String appendPath(String currentPath, String fieldName) {
        return currentPath.isEmpty() ? "deliveryContentAudit.get" + capitalize(fieldName) + "()" : currentPath + ".get" + capitalize(fieldName) + "()";
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


}
