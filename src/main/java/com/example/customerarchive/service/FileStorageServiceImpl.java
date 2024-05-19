package com.example.customerarchive.service;

import com.example.customerarchive.exception.CustomFileNotFoundException;
import com.example.customerarchive.exception.ResourceNotFoundException;
import com.example.customerarchive.model.File;
import com.example.customerarchive.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path fileStorageLocation;
    private final FileRepository fileRepository;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir, FileRepository fileRepository) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.fileRepository = fileRepository;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                logger.warn("Filename contains invalid path sequence {}", fileName);
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored file with name {}", fileName);
            return fileName;
        } catch (IOException ex) {
            logger.error("Could not store file {}", fileName, ex);
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found for this id :: " + fileId));
        try {
            Path filePath = Paths.get(file.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                logger.info("Loaded file as resource with id {}", fileId);
                return resource;
            } else {
                logger.error("File not found {}", file.getFilePath());
                throw new CustomFileNotFoundException("File not found " + file.getFilePath());
            }
        } catch (Exception ex) {
            logger.error("File not found {}", file.getFilePath(), ex);
            throw new CustomFileNotFoundException("File not found " + file.getFilePath(), ex);
        }
    }
}
