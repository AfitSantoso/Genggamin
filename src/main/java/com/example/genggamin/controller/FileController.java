package com.example.genggamin.controller;

import com.example.genggamin.dto.Base64FileRequest;
import com.example.genggamin.service.FileStorageService;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

  private static final Logger logger = LoggerFactory.getLogger(FileController.class);
  private final FileStorageService fileStorageService;

  public FileController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping("/upload-base64")
  public ResponseEntity<?> uploadBase64(@RequestBody Base64FileRequest request) {
    try {
      if (request.getBase64Content() == null || request.getFilename() == null) {
        return ResponseEntity.badRequest().body("Filename and Base64 content are required");
      }

      // Remove header if present (e.g., "data:image/png;base64,")
      String base64String = request.getBase64Content();
      if (base64String.contains(",")) {
        base64String = base64String.split(",")[1];
      }

      byte[] decodedBytes = Base64.getDecoder().decode(base64String);
      String storedPath = fileStorageService.storeFile(decodedBytes, request.getFilename());

      logger.info("File uploaded successfully via Base64: {}", storedPath);
      return ResponseEntity.ok().body("File uploaded successfully: " + storedPath);

    } catch (IllegalArgumentException e) {
      logger.error("Invalid Base64 input", e);
      return ResponseEntity.badRequest().body("Invalid Base64 content");
    } catch (Exception e) {
      logger.error("File upload failed", e);
      return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
    }
  }
}
