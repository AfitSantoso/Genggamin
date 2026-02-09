package com.example.genggamin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

  private final Path fileStorageLocation;

  public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new RuntimeException(
          "Could not create the directory where the uploaded files will be stored.", ex);
    }
  }

  public String storeFile(MultipartFile file, String targetFilename) {
    // Normalize file name
    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

    try {
      // Check if the file's name contains invalid characters
      if (originalFileName.contains("..")) {
        throw new RuntimeException(
            "Sorry! Filename contains invalid path sequence " + originalFileName);
      }

      // Determine extension
      String extension = "";
      int i = originalFileName.lastIndexOf('.');
      if (i > 0) {
        extension = originalFileName.substring(i);
      }

      String finalFileName = targetFilename + extension;

      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = this.fileStorageLocation.resolve(finalFileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return targetLocation.toString();
    } catch (IOException ex) {
      throw new RuntimeException(
          "Could not store file " + targetFilename + ". Please try again!", ex);
    }
  }

  public String storeFile(byte[] content, String targetFilename) {
    // Normalize file name
    String originalFileName = StringUtils.cleanPath(targetFilename);

    try {
      // Check if the file's name contains invalid characters
      if (originalFileName.contains("..")) {
        throw new RuntimeException(
            "Sorry! Filename contains invalid path sequence " + originalFileName);
      }

      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = this.fileStorageLocation.resolve(originalFileName);
      Files.write(targetLocation, content);

      return targetLocation.toString();
    } catch (IOException ex) {
      throw new RuntimeException(
          "Could not store file " + targetFilename + ". Please try again!", ex);
    }
  }

  public void deleteFile(String filePath) {
    if (filePath == null || filePath.isEmpty()) {
      return;
    }
    try {
      Path path = Paths.get(filePath);
      Files.deleteIfExists(path);
    } catch (IOException ex) {
      // Log warning but don't stop the process? Or throw?
      // Since it's cleanup, maybe just existing silently or logging is fine.
      // For now let's just print stack trace or ignore strictly to avoid blocking update
      logger.error("Could not delete file: {}. Error: {}", filePath, ex.getMessage());
    }
  }
}
