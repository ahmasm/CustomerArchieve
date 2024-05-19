package com.example.customerarchive.controller;

import com.example.customerarchive.exception.CustomFileNotFoundException;
import com.example.customerarchive.exception.ResourceNotFoundException;
import com.example.customerarchive.model.Customer;
import com.example.customerarchive.model.File;
import com.example.customerarchive.security.JwtUtil;
import com.example.customerarchive.service.CustomerService;
import com.example.customerarchive.service.FileService;
import com.example.customerarchive.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FileService fileService;

    @PostMapping("/upload/{customerId}")
    public ResponseEntity<?> uploadFile(@PathVariable Long customerId, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("User {} is attempting to upload a file for customer {}", username, customerId);
        if (!customerService.isCustomerOwnedByUser(customerId, username)) {
            logger.warn("User {} is not allowed to upload files for customer {}", username, customerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to upload files for this customer.");
        }
        File uploadedFile = fileService.addFile(customerId, file);
        logger.info("File uploaded successfully for customer {}", customerId);
        return ResponseEntity.ok(uploadedFile);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("User {} is attempting to delete file {}", username, fileId);
        if (!fileService.isFileOwnedByUser(fileId, username)) {
            logger.warn("User {} is not allowed to delete file {}", username, fileId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to delete this file.");
        }
        fileService.deleteFile(fileId);
        logger.info("File {} deleted successfully", fileId);
        return ResponseEntity.ok("File deleted successfully");
    }

    @PutMapping("/update/{fileId}")
    public ResponseEntity<?> updateFile(@PathVariable Long fileId, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("User {} is attempting to update file {}", username, fileId);
        if (!fileService.isFileOwnedByUser(fileId, username)) {
            logger.warn("User {} is not allowed to update file {}", username, fileId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to update this file.");
        }
        File updatedFile = fileService.updateFile(fileId, file);
        logger.info("File {} updated successfully", fileId);
        return ResponseEntity.ok(updatedFile);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("User {} is attempting to download file {}", username, fileId);
        try {
            if (!fileService.isFileOwnedByUser(fileId, username)) {
                logger.warn("User {} is not allowed to download file {}", username, fileId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to download this file.");
            }
            Resource resource = fileService.loadFileAsResource(fileId);

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.error("Could not determine file type for file {}", fileId, ex);
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (ResourceNotFoundException | CustomFileNotFoundException ex) {
            logger.error("File not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getFilesForCustomer(@PathVariable Long customerId, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("User {} is attempting to get files for customer {}", username, customerId);
        if (!customerService.isCustomerOwnedByUser(customerId, username)) {
            logger.warn("User {} is not allowed to get files for customer {}", username, customerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to get files for this customer.");
        }
        List<File> files = fileService.getAllFilesForCustomer(customerId);
        logger.info("Found {} files for customer {}", files.size(), customerId);
        return ResponseEntity.ok(files);
    }
}
