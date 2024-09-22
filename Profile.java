package com.chat.services;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Profile {

    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class JsonEncryptionExample {

    public static void main(String[] args) {
        String filePath = "path/to/your/file.json"; // Specify your JSON file path

        try {
            SecretKey secretKey = generateKey();

            // Read JSON from file
            String jsonData = readFile(filePath);
            System.out.println("Original JSON: " + jsonData);

            // Encrypt the JSON data
            String encryptedData = encrypt(jsonData, secretKey);
            System.out.println("Encrypted Data: " + encryptedData);

            // Write encrypted data back to the same file
            writeFile(filePath, encryptedData);

            // To demonstrate decryption, read the encrypted data back
            String encryptedDataFromFile = readFile(filePath);
            String decryptedData = decrypt(encryptedDataFromFile, secretKey);
            System.out.println("Decrypted JSON: " + decryptedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // You can use 192 or 256 bits
        return keyGen.generateKey();
    }

    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    public static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static void writeFile(String filePath, String data) throws IOException {
        Files.write(Paths.get(filePath), data.getBytes());
    }
}

}
