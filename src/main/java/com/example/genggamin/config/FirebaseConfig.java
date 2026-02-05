package com.example.genggamin.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  @Value("${firebase.config.path:firebase-service-account.json}")
  private String firebaseConfigPath;

  @PostConstruct
  public void initialize() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        InputStream serviceAccount = null;
        try {
          // Try to load from classpath
          serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();
        } catch (Exception e) {
          // Try to load from file system
          try {
            serviceAccount = new FileInputStream(firebaseConfigPath);
          } catch (Exception ex) {
            System.out.println("Firebase config file not found. Push notifications will not work.");
            return;
          }
        }

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        System.out.println("Firebase application has been initialized");
      }
    } catch (IOException e) {
      System.err.println("Error initializing Firebase: " + e.getMessage());
    }
  }
}
