package org.example.project_java_service.service.impl;

import org.example.project_java_service.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation = Paths.get("uploads/cvs").toAbsolutePath().normalize();

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ CV.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Kiểm tra định dạng bắt buộc là PDF
        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Chỉ cho phép tải lên file định dạng PDF!");
        }

        try {
            // Đổi tên file thành chuỗi ngẫu nhiên để không bị trùng lặp tên
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file vào thư mục đích
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn của file
            return "uploads/cvs/" + newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Có lỗi xảy ra khi lưu file CV. Vui lòng thử lại!", ex);
        }
    }
}