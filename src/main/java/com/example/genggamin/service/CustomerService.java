package com.example.genggamin.service;

import com.example.genggamin.dto.*;
import com.example.genggamin.entity.Customer;
import com.example.genggamin.entity.EmergencyContact;
import com.example.genggamin.entity.User;
import com.example.genggamin.enums.DocumentType;
import com.example.genggamin.exception.ResourceNotFoundException;
import com.example.genggamin.exception.ValidationException;
import com.example.genggamin.repository.CustomerRepository;
import com.example.genggamin.repository.EmergencyContactRepository;
import com.example.genggamin.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;

  @Transactional
  public CustomerResponse createOrUpdateCustomer(
      Long userId, CustomerRequest request, MultipartFile fileKtp, MultipartFile fileSelfie, MultipartFile filePayslip) {
    
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    Customer existingCustomer = customerRepository.findByUserId(userId).orElse(null);
    boolean isNew = existingCustomer == null;

    if (isNew) {
       if (fileKtp == null || fileKtp.isEmpty() || fileSelfie == null || fileSelfie.isEmpty()) {
          throw new ValidationException("KTP and Selfie photos are mandatory for new customer profile.");
       }
    }

    validateNikUniqueness(request.getNik(), userId);

    Customer customer = isNew ? new Customer() : existingCustomer;
    updateCustomerEntity(customer, user, request); // Helper method to populate fields
    
    // Handle File Uploads
    handleFileUploads(customer, user, request.getNik(), fileKtp, fileSelfie, filePayslip);

    // Validate if any mandatory image path is missing (double check)
    if (isNew && (customer.getKtpImagePath() == null || customer.getSelfieImagePath() == null)) {
         throw new ValidationException("Failed to upload mandatory documents.");
    }

    customer = customerRepository.save(customer);

    updateEmergencyContact(customer, request.getEmergencyContact());

    return mapToCustomerResponse(customer, user);
  }

  public CustomerResponse getCustomerByUserId(Long userId) {
    Customer customer = customerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user id: " + userId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return mapToCustomerResponse(customer, user);
  }

  public boolean hasCustomerData(Long userId) {
    return customerRepository.existsByUserId(userId);
  }

  private void validateNikUniqueness(String nik, Long userId) {
    customerRepository.findByNik(nik).ifPresent(existingCustomer -> {
      if (!existingCustomer.getUserId().equals(userId)) {
        throw new ValidationException("NIK " + nik + " is already registered by another customer");
      }
    });
  }

  private void updateCustomerEntity(Customer customer, User user, CustomerRequest request) {
    customer.setUserId(user.getId());
    customer.setNik(request.getNik());
    customer.setDateOfBirth(request.getDateOfBirth());
    customer.setPlaceOfBirth(request.getPlaceOfBirth());
    customer.setAddress(request.getAddress());
    customer.setCurrentAddress(request.getCurrentAddress());
    customer.setPhone(request.getPhone());
    customer.setMonthlyIncome(request.getMonthlyIncome());
    customer.setOccupation(request.getOccupation());
    customer.setMotherMaidenName(request.getMotherMaidenName());
  }

  private void handleFileUploads(Customer customer, User user, String nik, MultipartFile fileKtp, MultipartFile fileSelfie, MultipartFile filePayslip) {
    String sanitizedFullName = user.getFullName().replaceAll("\\s+", "_");
    String fileBaseName = sanitizedFullName + "-" + nik;

    if (fileKtp != null && !fileKtp.isEmpty()) {
        String ktpPath = fileStorageService.storeFile(fileKtp, fileBaseName + "_" + DocumentType.KTP.name());
        customer.setKtpImagePath(ktpPath);
    }
    
    if (fileSelfie != null && !fileSelfie.isEmpty()) {
        String selfiePath = fileStorageService.storeFile(fileSelfie, fileBaseName + "_" + DocumentType.SELFIE.name());
        customer.setSelfieImagePath(selfiePath);
    }

    if (filePayslip != null && !filePayslip.isEmpty()) {
        String payslipPath = fileStorageService.storeFile(filePayslip, fileBaseName + "_" + DocumentType.PAYSLIP.name());
        customer.setPayslipImagePath(payslipPath);
    }
  }

  private void updateEmergencyContact(Customer customer, EmergencyContactRequest ecRequest) {
    if (ecRequest != null) {
      emergencyContactRepository.deleteByCustomerId(customer.getId());
      emergencyContactRepository.flush();

      EmergencyContact ec = new EmergencyContact();
      ec.setCustomerId(customer.getId());
      ec.setContactName(ecRequest.getName());
      ec.setContactPhone(ecRequest.getPhone());
      ec.setRelationship(ecRequest.getRelationship());
      emergencyContactRepository.save(ec);
    }
  }

  private CustomerResponse mapToCustomerResponse(Customer customer, User user) {
    CustomerResponse response = new CustomerResponse();
    response.setId(customer.getId());
    response.setUserId(customer.getUserId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setFullName(user.getFullName());
    response.setNik(customer.getNik());
    response.setAddress(customer.getAddress());
    response.setDateOfBirth(customer.getDateOfBirth());
    response.setPlaceOfBirth(customer.getPlaceOfBirth());
    response.setMonthlyIncome(customer.getMonthlyIncome());
    response.setOccupation(customer.getOccupation());
    response.setCustomerPhone(customer.getPhone());
    response.setCurrentAddress(customer.getCurrentAddress());
    response.setMotherMaidenName(customer.getMotherMaidenName());
    response.setKtpImagePath(customer.getKtpImagePath());
    response.setSelfieImagePath(customer.getSelfieImagePath());
    response.setPayslipImagePath(customer.getPayslipImagePath());
    response.setCreatedAt(customer.getCreatedAt());

    List<EmergencyContact> emergencyContacts = emergencyContactRepository.findByCustomerId(customer.getId());
    List<EmergencyContactResponse> ecResponses = emergencyContacts.stream()
            .map(ec -> {
                  EmergencyContactResponse ecr = new EmergencyContactResponse();
                  ecr.setId(ec.getId());
                  ecr.setContactName(ec.getContactName());
                  ecr.setContactPhone(ec.getContactPhone());
                  ecr.setRelationship(ec.getRelationship());
                  ecr.setCreatedAt(ec.getCreatedAt());
                  return ecr;
                })
            .collect(Collectors.toList());

    response.setEmergencyContacts(ecResponses);
    return response;
  }
}
