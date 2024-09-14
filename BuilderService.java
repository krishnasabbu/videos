package com.chat.services;

import org.json.JSONObject;

public class BuilderService {

    public static String generateBuildMethod(String sourceClass, String targetClass, JSONObject sourceJson, JSONObject targetJson) {
        String targetVariable = decapitalize(targetClass);  // e.g., "target" for "Employee"

        StringBuilder methodCode = new StringBuilder();
        methodCode.append("public ").append(targetClass).append(" build").append(targetClass).append("(").append(sourceClass).append(" source) {\n");
        methodCode.append("    ").append(targetClass).append(" ").append(targetVariable).append(" = new ").append(targetClass).append("();\n");

        // Iterate through the target JSON fields
        for (Object targetField : targetJson.keySet()) {
            JSONObject targetFieldJson = targetJson.getJSONObject(targetField.toString());
            if (targetFieldJson.has("mapping")) {
                String sourceFieldPath = targetFieldJson.getString("mapping");
                String[] sourceFields = sourceFieldPath.split("\\.");
                String sourceField = sourceFields[0]; // For the nested field, get the top-level object

                methodCode.append("    ").append(targetVariable).append(".set").append(capitalize(targetField.toString())).append("(source.get");
                methodCode.append(capitalize(sourceField)).append("()");

                // Handle nested fields if present
                if (sourceFields.length > 1) {
                    for (int i = 1; i < sourceFields.length; i++) {
                        methodCode.append(".get").append(capitalize(sourceFields[i])).append("()");
                    }
                }

                methodCode.append(");\n");
            }
        }

        methodCode.append("    return ").append(targetVariable).append(";\n");
        methodCode.append("}\n");
        return methodCode.toString();
    }

    public static String generateBuildMethodForList(String sourceClass, String targetClass, JSONObject sourceJson, JSONObject targetJson) {
        String targetVariable = decapitalize(targetClass);  // e.g., "target" for "Employee"
        String targetListVariable = decapitalize(targetClass) + "List"; // e.g., "targetList" for "EmployeeList"

        StringBuilder methodCode = new StringBuilder();
        methodCode.append("public List<").append(targetClass).append("> build").append(targetClass).append("List(List<").append(sourceClass).append("> sourceList) {\n");
        methodCode.append("    List<").append(targetClass).append("> ").append(targetListVariable).append(" = new ArrayList<>();\n");
        methodCode.append("    for (").append(sourceClass).append(" source : sourceList) {\n");
        methodCode.append("        ").append(targetClass).append(" ").append(targetVariable).append(" = new ").append(targetClass).append("();\n");

        // Iterate through the target JSON fields
        for (Object targetField : targetJson.keySet()) {
            JSONObject targetFieldJson = targetJson.getJSONObject(targetField.toString());
            if (targetFieldJson.has("mapping")) {
                String sourceFieldPath = targetFieldJson.getString("mapping");
                String[] sourceFields = sourceFieldPath.split("\\.");
                String sourceField = sourceFields[0]; // For the nested field, get the top-level object

                methodCode.append("        ").append(targetVariable).append(".set").append(capitalize(targetField.toString())).append("(source.get");
                methodCode.append(capitalize(sourceField)).append("()");

                // Handle nested fields if present
                if (sourceFields.length > 1) {
                    for (int i = 1; i < sourceFields.length; i++) {
                        methodCode.append(".get").append(capitalize(sourceFields[i])).append("()");
                    }
                }

                methodCode.append(");\n");
            }
        }

        methodCode.append("        ").append(targetListVariable).append(".add(").append(targetVariable).append(");\n");
        methodCode.append("    }\n");
        methodCode.append("    return ").append(targetListVariable).append(";\n");
        methodCode.append("}\n");
        return methodCode.toString();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static void main(String[] args) {
        // Define JSON strings for source and target
        String sourceJsonStr = "{\n" +
                "  \"profile\": {\n" +
                "    \"name\": {\n" +
                "      \"type\": \"String\"\n" +
                "    },\n" +
                "    \"age\": {\n" +
                "      \"type\": \"int\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"details\": {\n" +
                "    \"address\": {\n" +
                "      \"type\": \"String\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String targetJsonStr = "{\n" +
                "  \"fullName\": {\n" +
                "    \"type\": \"String\",\n" +
                "    \"mapping\": \"profile.name\"\n" +
                "  },\n" +
                "  \"birth\": {\n" +
                "    \"type\": \"int\",\n" +
                "    \"mapping\": \"profile.age\"\n" +
                "  },\n" +
                "  \"address\": {\n" +
                "    \"type\": \"String\",\n" +
                "    \"mapping\": \"details.address\"\n" +
                "  }\n" +
                "}";

        // Convert JSON strings to JSONObject
        JSONObject sourceJson = new JSONObject(sourceJsonStr);
        JSONObject targetJson = new JSONObject(targetJsonStr);

        // Generate the buildEmployee method
        String methodCode = generateBuildMethod("Student", "Employee", sourceJson, targetJson);
        System.out.println(methodCode);

        String methodListCode = generateBuildMethodForList("Student", "Employee", sourceJson, targetJson);
        System.out.println(methodListCode);
    }
}
