package com.example.genggamin.service;

import com.example.genggamin.dto.*;
import com.example.genggamin.entity.Customer;
import com.example.genggamin.entity.EmergencyContact;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.CustomerRepository;
import com.example.genggamin.repository.EmergencyContactRepository;
import com.example.genggamin.repository.UserRepository;
import java.util.ArrayList;
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
    // Validasi user exists
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    // Check files requirements
    // If it is a new customer or updating files, they must be present if we enforce "wajib ada"
    // However, for update, we might allow skipping if they already have one.
    // Based on user prompt: "sifatnya juga harus wajib ada" (must be mandatory).
    
    // Cari customer
    Customer existingCustomerOrNew = customerRepository.findByUserId(userId).orElse(null);
    boolean isNew = existingCustomerOrNew == null;

    if (isNew) {
       if (fileKtp == null || fileKtp.isEmpty() || fileSelfie == null || fileSelfie.isEmpty()) {
          throw new RuntimeException("KTP and Selfie photos are mandatory for new profile profile.");
       }
       // Payslip might be mandatory too, usually for loan application, but let's make it optional for profile creation if not strictly enforced yet.
       // User said "ingin menambahkan juga ... sifatnya juga harus wajib ada" in previous request for KTP/Selfie.
       // For Payslip "ingin payslip yang berupa gambar juga". Assumed optional unless specified.
       // But wait, usually KYC requires proof of income. Let's make it optional for now to avoid blocking if they don't have it handy, or consistent with KTP/Selfie if requested.
       // Re-reading: "sifatnya juga harus wajib ada" was for KTP and Selfie in previous turn.
       // Current turn: "ingin payslip yang berupa gambar juga ... tetapi penamaannya adalah fullname_nik_PAYSLIP".
       // I'll keep it optional in validation but saving it if present.
    }

    // Cek apakah NIK sudah digunakan oleh customer lain
    customerRepository
        .findByNik(request.getNik())
        .ifPresent(
            existingCustomer -> {
              if (!existingCustomer.getUserId().equals(userId)) {
                throw new RuntimeException("NIK already registered by another customer");
              }
            });

    // Cari atau buat customer baru
    Customer customer = isNew ? new Customer() : existingCustomerOrNew;

    customer.setUserId(userId);
    // ... existing fields ...
    customer.setNik(request.getNik());
    customer.setDateOfBirth(request.getDateOfBirth());
    customer.setPlaceOfBirth(request.getPlaceOfBirth());
    customer.setAddress(request.getAddress());
    customer.setCurrentAddress(request.getCurrentAddress());
    customer.setPhone(request.getPhone());
    
    // Handle File Uploads
    String sanitizedFullName = user.getFullName().replaceAll("\\s+", "_"); // Replace spaces
    String fileBaseName = sanitizedFullName + "-" + request.getNik();

    if (fileKtp != null && !fileKtp.isEmpty()) {
        String ktpPath = fileStorageService.storeFile(fileKtp, fileBaseName + "_KTP");
        customer.setKtpImagePath(ktpPath);
    }
    
    if (fileSelfie != null && !fileSelfie.isEmpty()) {
        String selfiePath = fileStorageService.storeFile(fileSelfie, fileBaseName + "_SELFIE");
        customer.setSelfieImagePath(selfiePath);
    }

    if (filePayslip != null && !filePayslip.isEmpty()) {
        String payslipPath = fileStorageService.storeFile(filePayslip, fileBaseName + "_PAYSLIP");
        customer.setPayslipImagePath(payslipPath);
    }
    
    // Ensure paths are set if they were null (though strict validation above handles new)
    if (isNew && (customer.getKtpImagePath() == null || customer.getSelfieImagePath() == null)) {
         throw new RuntimeException("Failed to upload mandatory documents.");
    }

    customer.setMonthlyIncome(request.getMonthlyIncome());
    customer.setOccupation(request.getOccupation());
    customer.setMotherMaidenName(request.getMotherMaidenName());

    customer = customerRepository.save(customer);

    // Update emergency contact (singular)
    if (request.getEmergencyContact() != null) {
      // Hapus kontak darurat yang lama
      emergencyContactRepository.deleteByCustomerId(customer.getId());
      emergencyContactRepository.flush();

      // Tambahkan kontak darurat yang baru
      EmergencyContactRequest ecRequest = request.getEmergencyContact();
      EmergencyContact ec = new EmergencyContact();
      ec.setCustomerId(customer.getId());
      ec.setContactName(ecRequest.getName());
      ec.setContactPhone(ecRequest.getPhone());
      ec.setRelationship(ecRequest.getRelationship());
      emergencyContactRepository.save(ec);
    }

    return mapToCustomerResponse(customer, user);
  }

  public CustomerResponse getCustomerByUserId(Long userId) {
    Customer customer =
        customerRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Customer data not found"));

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    return mapToCustomerResponse(customer, user);
  }

  public boolean hasCustomerData(Long userId) {
    return customerRepository.existsByUserId(userId);
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

    // Get emergency contacts
    List<EmergencyContact> emergencyContacts =
        emergencyContactRepository.findByCustomerId(customer.getId());
    List<EmergencyContactResponse> ecResponses =
        emergencyContacts.stream()
            .map(
                ec -> {
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
