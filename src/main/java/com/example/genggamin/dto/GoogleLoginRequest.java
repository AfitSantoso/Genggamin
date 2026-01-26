package com.example.genggamin.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
    private String fcmToken;
}
