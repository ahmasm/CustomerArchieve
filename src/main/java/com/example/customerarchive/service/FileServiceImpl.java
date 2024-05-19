package com.example.customerarchive.service;

import com.example.customerarchive.exception.FileStorageException;
import com.example.customerarchive.exception.ResourceNotFoundException;
import com.example.customerarchive.model.Customer;
import com.example.customerarchive.model.File;
import com.example.customerarchive.repository.CustomerRepository;
import com.example.customerarchive.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final CustomerRepository customerRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public FileServiceImpl(FileRepository fileRepository, FileStorageService fileStorageService, CustomerRepository customerRepository) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<File> getAllFilesForCustomer(Long customerId) {
        logger.info("Fetching all files for customer id: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for this id :: " + customerId));
        List<File> files = customer.getFiles().stream().collect(Collectors.toList());
        logger.info("Found {} files for customer id: {}", files.size(), customerId);
        return files;
    }

    @Transactional
    public File addFile(Long customerId, MultipartFile file) {
        logger.info("Adding file for customer id: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for this id :: " + customerId));

        String fileName = fileStorageService.storeFile(file);

        File dbFile = new File();
        dbFile.setFileName(fileName);
        dbFile.setFilePath(uploadDir + fileName);
        dbFile.setFileType(file.getContentType());
        dbFile.setCustomer(customer);
        dbFile.setUploadDate(new Date());
        dbFile.setUpdateDate(new Date());
        File savedFile = fileRepository.save(dbFile);
        logger.info("File added successfully for customer id: {}", customerId);
        return savedFile;
    }

    @Transactional
    public void deleteFile(Long fileId) {
        logger.info("Deleting file id: {}", fileId);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));
        fileRepository.delete(file);
        try {
            Path filePath = Paths.get(file.getFilePath()).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
            logger.info("File deleted from filesystem: {}", filePath);
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", file.getFilePath(), ex);
            throw new FileStorageException("Could not delete file: " + file.getFilePath(), ex);
        }
    }

    @Transactional
    public File updateFile(Long fileId, MultipartFile file) {
        logger.info("Updating file id: {}", fileId);
        File existingFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found for this id :: " + fileId));

        try {
            Path existingFilePath = Paths.get(existingFile.getFilePath()).toAbsolutePath().normalize();
            Files.deleteIfExists(existingFilePath);
            logger.info("Deleted existing file from filesystem: {}", existingFilePath);
        } catch (IOException ex) {
            logger.error("Could not delete existing file: {}", existingFile.getFilePath(), ex);
            throw new FileStorageException("Could not delete existing file: " + existingFile.getFilePath(), ex);
        }

        String newFileName = fileStorageService.storeFile(file);

        existingFile.setFileName(newFileName);
        existingFile.setFilePath(uploadDir + newFileName);
        existingFile.setFileType(file.getContentType());
        existingFile.setUpdateDate(new Date());
        File updatedFile = fileRepository.save(existingFile);
        logger.info("File updated successfully for file id: {}", fileId);
        return updatedFile;
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long fileId) {
        logger.info("Loading file as resource for file id: {}", fileId);
        return fileStorageService.loadFileAsResource(fileId);
    }

    @Transactional(readOnly = true)
    public boolean isFileOwnedByUser(Long fileId, String username) {
        logger.info("Checking ownership of file id: {} for username: {}", fileId, username);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found for this id : " + fileId));
        boolean isOwned = file.getCustomer().getUser().getUsername().equals(username);
        logger.info("File id: {} is owned by username: {}: {}", fileId, username, isOwned);
        return isOwned;
    }
}
