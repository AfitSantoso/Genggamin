package com.example.genggamin.service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DigitalSignatureService {

  private static final Logger logger = LoggerFactory.getLogger(DigitalSignatureService.class);
  private PrivateKey privateKey;
  private PublicKey publicKey;

  @PostConstruct
  public void init() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair pair = keyGen.generateKeyPair();
      this.privateKey = pair.getPrivate();
      this.publicKey = pair.getPublic();
      logger.info("Digital Signature Service initialized with new RSA KeyPair");
    } catch (Exception e) {
      logger.error("Error initializing DigitalSignatureService", e);
      throw new RuntimeException(e);
    }
  }

  public String sign(String data) throws Exception {
    Signature privateSignature = Signature.getInstance("SHA256withRSA");
    privateSignature.initSign(this.privateKey);
    privateSignature.update(data.getBytes(StandardCharsets.UTF_8));

    byte[] signature = privateSignature.sign();
    return Base64.getEncoder().encodeToString(signature);
  }

  public boolean verify(String data, String signature) throws Exception {
    Signature publicSignature = Signature.getInstance("SHA256withRSA");
    publicSignature.initVerify(this.publicKey);
    publicSignature.update(data.getBytes(StandardCharsets.UTF_8));

    byte[] signatureBytes = Base64.getDecoder().decode(signature);
    return publicSignature.verify(signatureBytes);
  }

  public String getPublicKey() {
    return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
  }
}
