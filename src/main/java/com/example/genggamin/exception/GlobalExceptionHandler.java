package com.example.genggamin.exception;

import com.example.genggamin.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(JsonProcessingException.class)
  public ResponseEntity<ApiResponse<Object>> handleJsonProcessingException(
      JsonProcessingException e) {
    ApiResponse<Object> response =
        new ApiResponse<>(false, "Invalid JSON format: " + e.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
      ResourceNotFoundException e) {
    ApiResponse<Object> response = new ApiResponse<>(false, e.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException e) {
    ApiResponse<Object> response = new ApiResponse<>(false, e.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
    ApiResponse<Object> response = new ApiResponse<>(false, e.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception e) {
    ApiResponse<Object> response =
        new ApiResponse<>(false, "Internal Server Error: " + e.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
