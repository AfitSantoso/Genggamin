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

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final UserRepository userRepository;

  @Transactional
  public CustomerResponse createOrUpdateCustomer(Long userId, CustomerRequest request) {
    // Validasi user exists
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

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
    Customer customer = customerRepository.findByUserId(userId).orElse(new Customer());

    customer.setUserId(userId);
    customer.setNik(request.getNik());
    customer.setAddress(request.getAddress());
    customer.setDateOfBirth(request.getDateOfBirth());
    customer.setMonthlyIncome(request.getMonthlyIncome());
    customer.setFullName(request.getFullName());
    customer.setPhone(request.getPhone());
    customer.setCurrentAddress(request.getCurrentAddress());
    customer.setMotherMaidenName(request.getMotherMaidenName());

    customer = customerRepository.save(customer);

    // Update emergency contacts
    if (request.getEmergencyContacts() != null && !request.getEmergencyContacts().isEmpty()) {
      // Hapus kontak darurat yang lama
      emergencyContactRepository.deleteByCustomerId(customer.getId());
      emergencyContactRepository.flush();

      // Tambahkan kontak darurat yang baru
      List<EmergencyContact> emergencyContacts = new ArrayList<>();
      for (EmergencyContactRequest ecRequest : request.getEmergencyContacts()) {
        EmergencyContact ec = new EmergencyContact();
        ec.setCustomerId(customer.getId());
        ec.setContactName(ecRequest.getContactName());
        ec.setContactPhone(ecRequest.getContactPhone());
        ec.setRelationship(ecRequest.getRelationship());
        emergencyContacts.add(ec);
      }
      emergencyContactRepository.saveAll(emergencyContacts);
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
    response.setNik(customer.getNik());
    response.setAddress(customer.getAddress());
    response.setDateOfBirth(customer.getDateOfBirth());
    response.setMonthlyIncome(customer.getMonthlyIncome());
    response.setCustomerFullName(customer.getFullName());
    response.setCustomerPhone(customer.getPhone());
    response.setCurrentAddress(customer.getCurrentAddress());
    response.setMotherMaidenName(customer.getMotherMaidenName());
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
