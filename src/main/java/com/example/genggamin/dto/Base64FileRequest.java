package com.example.genggamin.dto;

public class Base64FileRequest {
  private String filename;
  private String base64Content;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getBase64Content() {
    return base64Content;
  }

  public void setBase64Content(String base64Content) {
    this.base64Content = base64Content;
  }
}
