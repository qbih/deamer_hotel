package com.example.dulich.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    
    String uploadImage(MultipartFile file, String directory) throws Exception;
    
    boolean deleteImage(String imagePath);
}
