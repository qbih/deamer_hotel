package com.example.dulich.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.dulich.service.FileUploadService;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private final String UPLOAD_DIR = "src/main/resources/static/";

    @Override
    public String uploadImage(MultipartFile file, String directory) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // Tạo đường dẫn đến thư mục lưu trữ
            String uploadPath = UPLOAD_DIR + directory;
            Path dirPath = Paths.get(uploadPath);
            
            // Tạo thư mục nếu không tồn tại
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // Tạo tên file độc nhất để tránh trùng lặp
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = dirPath.resolve(uniqueFilename);
            
            // Lưu file
            Files.copy(file.getInputStream(), filePath);
            
            // Trả về đường dẫn tương đối để lưu vào database
            return directory + "/" + uniqueFilename;
            
        } catch (IOException e) {
            throw new Exception("Không thể tải lên tệp: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        
        try {
            // Tạo đường dẫn đầy đủ đến file hình ảnh
            Path filePath = Paths.get(UPLOAD_DIR).resolve(imagePath);
            
            // Kiểm tra xem file có tồn tại không
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
