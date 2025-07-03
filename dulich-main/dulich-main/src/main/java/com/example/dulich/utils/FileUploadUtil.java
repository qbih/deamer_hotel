package com.example.dulich.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadUtil {

    public static String saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
        // Generate a unique file name to prevent overwriting existing files
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // Get the absolute path from project root
        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException e) {
            throw new IOException("Could not save file: " + fileName, e);
        }
    }

    public static void deleteFile(String filePath) {
        try {
            // If the path is a web path (starts with /uploads/), convert to absolute path
            if (filePath.startsWith("/uploads/")) {
                String projectRoot = System.getProperty("user.dir");
                filePath = projectRoot + filePath.replace("/", "\\");
            }

            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log error but don't throw exception
            e.printStackTrace();
        }
    }
}
