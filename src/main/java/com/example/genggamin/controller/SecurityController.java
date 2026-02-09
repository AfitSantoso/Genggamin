package com.example.genggamin.controller;

import com.example.genggamin.dto.DigitallySignRequest;
import com.example.genggamin.dto.VerifySignatureRequest;
import com.example.genggamin.service.DigitalSignatureService;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

  private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
  private final DigitalSignatureService digitalSignatureService;

  public SecurityController(DigitalSignatureService digitalSignatureService) {
    this.digitalSignatureService = digitalSignatureService;
  }

  @PostMapping("/sign")
  public ResponseEntity<?> signData(@RequestBody DigitallySignRequest request) {
    try {
      String signature = digitalSignatureService.sign(request.getData());
      return ResponseEntity.ok(Collections.singletonMap("signature", signature));
    } catch (Exception e) {
      logger.error("Error signing data", e);
      return ResponseEntity.internalServerError().body("Error signing data");
    }
  }

  @PostMapping("/verify")
  public ResponseEntity<?> verifyData(@RequestBody VerifySignatureRequest request) {
    try {
      boolean isValid = digitalSignatureService.verify(request.getData(), request.getSignature());
      return ResponseEntity.ok(Collections.singletonMap("isValid", isValid));
    } catch (Exception e) {
      logger.error("Error verifying data", e);
      return ResponseEntity.internalServerError().body("Error verifying data");
    }
  }

  @GetMapping("/public-key")
  public ResponseEntity<?> getPublicKey() {
    return ResponseEntity.ok(
        Collections.singletonMap("publicKey", digitalSignatureService.getPublicKey()));
  }
}
