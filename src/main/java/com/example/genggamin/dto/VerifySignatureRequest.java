package com.example.genggamin.dto;

public class VerifySignatureRequest {
  private String data;
  private String signature;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }
}
