package com.example.customerarchive.service;


import com.example.customerarchive.model.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<File> getAllFilesForCustomer(Long customerId);
    File addFile(Long customerId, MultipartFile file);
    void deleteFile(Long fileId);
    File updateFile(Long fileId, MultipartFile file);
    Resource loadFileAsResource(Long fileId);
    boolean isFileOwnedByUser(Long fileId, String username);
}
