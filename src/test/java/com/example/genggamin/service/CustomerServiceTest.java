package com.example.genggamin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.genggamin.dto.CustomerRequest;
import com.example.genggamin.dto.CustomerResponse;
import com.example.genggamin.dto.EmergencyContactRequest;
import com.example.genggamin.entity.Customer;
import com.example.genggamin.entity.EmergencyContact;
import com.example.genggamin.entity.User;
import com.example.genggamin.exception.ResourceNotFoundException;
import com.example.genggamin.exception.ValidationException;
import com.example.genggamin.repository.CustomerRepository;
import com.example.genggamin.repository.EmergencyContactRepository;
import com.example.genggamin.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Unit Tests for CustomerService
 *
 * <p>Best Practices yang diikuti:
 *
 * <ul>
 *   <li>Menggunakan Mockito (bukan @SpringBootTest) untuk kecepatan eksekusi
 *   <li>AAA Pattern (Arrange, Act, Assert)
 *   <li>Satu skenario per metode @Test
 *   <li>Penamaan deskriptif dengan pola givenXxx_whenYyy_thenZzz()
 *   <li>Nested class untuk mengelompokkan test berdasarkan method
 *   <li>Mock untuk Repository dan FileStorageService
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private EmergencyContactRepository emergencyContactRepository;

  @Mock private UserRepository userRepository;

  @Mock private FileStorageService fileStorageService;

  @InjectMocks private CustomerService customerService;

  private User testUser;
  private Customer testCustomer;
  private CustomerRequest testRequest;
  private MultipartFile mockKtpFile;
  private MultipartFile mockSelfieFile;

  @BeforeEach
  void setUp() {
    // Arrange: Setup common test data
    testUser =
        User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .build();

    testCustomer = new Customer();
    testCustomer.setId(1L);
    testCustomer.setUserId(1L);
    testCustomer.setNik("1234567890123456");
    testCustomer.setDateOfBirth(LocalDate.of(1990, 1, 1));
    testCustomer.setPlaceOfBirth("Jakarta");
    testCustomer.setAddress("Jl. Test No. 1");
    testCustomer.setCurrentAddress("Jl. Test No. 1");
    testCustomer.setPhone("081234567890");
    testCustomer.setMonthlyIncome(new BigDecimal("10000000"));
    testCustomer.setOccupation("Engineer");
    testCustomer.setMotherMaidenName("Mother Name");
    testCustomer.setAccountNumber("1234567890");
    testCustomer.setAccountHolderName("Test User");
    testCustomer.setKtpImagePath("/uploads/ktp_image.jpg");
    testCustomer.setSelfieImagePath("/uploads/selfie_image.jpg");
    testCustomer.setCreatedAt(LocalDateTime.now());

    testRequest = new CustomerRequest();
    testRequest.setNik("1234567890123456");
    testRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
    testRequest.setPlaceOfBirth("Jakarta");
    testRequest.setAddress("Jl. Test No. 1");
    testRequest.setCurrentAddress("Jl. Test No. 1");
    testRequest.setPhone("081234567890");
    testRequest.setMonthlyIncome(new BigDecimal("10000000"));
    testRequest.setOccupation("Engineer");
    testRequest.setMotherMaidenName("Mother Name");
    testRequest.setAccountNumber("1234567890");
    testRequest.setAccountHolderName("Test User");

    // Create mock files
    mockKtpFile = new MockMultipartFile("ktp", "ktp.jpg", "image/jpeg", "ktp content".getBytes());
    mockSelfieFile =
        new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "selfie content".getBytes());
  }

  // =========================================================================
  // Tests for getCustomerByUserId()
  // =========================================================================
  @Nested
  @DisplayName("getCustomerByUserId()")
  class GetCustomerByUserIdTests {

    @Test
    @DisplayName("should return CustomerResponse when customer and user exist")
    void givenValidUserId_whenGetCustomerByUserId_thenReturnCustomerResponse() {
      // Arrange
      given(customerRepository.findByUserId(1L)).willReturn(Optional.of(testCustomer));
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(emergencyContactRepository.findByCustomerId(1L)).willReturn(List.of());

      // Act
      CustomerResponse result = customerService.getCustomerByUserId(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getNik()).isEqualTo("1234567890123456");
      assertThat(result.getFullName()).isEqualTo("Test User");
      assertThat(result.getEmail()).isEqualTo("test@example.com");
      verify(customerRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when customer not found")
    void givenNonExistentCustomer_whenGetCustomerByUserId_thenThrowException() {
      // Arrange
      given(customerRepository.findByUserId(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> customerService.getCustomerByUserId(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Customer profile not found");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user not found")
    void givenNonExistentUser_whenGetCustomerByUserId_thenThrowException() {
      // Arrange
      given(customerRepository.findByUserId(1L)).willReturn(Optional.of(testCustomer));
      given(userRepository.findById(1L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> customerService.getCustomerByUserId(1L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User not found");
    }
  }

  // =========================================================================
  // Tests for getCustomerById()
  // =========================================================================
  @Nested
  @DisplayName("getCustomerById()")
  class GetCustomerByIdTests {

    @Test
    @DisplayName("should return CustomerResponse when customer exists")
    void givenValidCustomerId_whenGetCustomerById_thenReturnCustomerResponse() {
      // Arrange
      given(customerRepository.findById(1L)).willReturn(Optional.of(testCustomer));
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(emergencyContactRepository.findByCustomerId(1L)).willReturn(List.of());

      // Act
      CustomerResponse result = customerService.getCustomerById(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getNik()).isEqualTo("1234567890123456");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when customer not found")
    void givenNonExistentCustomerId_whenGetCustomerById_thenThrowException() {
      // Arrange
      given(customerRepository.findById(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> customerService.getCustomerById(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Customer not found");
    }
  }

  // =========================================================================
  // Tests for hasCustomerData()
  // =========================================================================
  @Nested
  @DisplayName("hasCustomerData()")
  class HasCustomerDataTests {

    @Test
    @DisplayName("should return true when customer data exists")
    void givenExistingCustomer_whenHasCustomerData_thenReturnTrue() {
      // Arrange
      given(customerRepository.existsByUserId(1L)).willReturn(true);

      // Act
      boolean result = customerService.hasCustomerData(1L);

      // Assert
      assertThat(result).isTrue();
      verify(customerRepository, times(1)).existsByUserId(1L);
    }

    @Test
    @DisplayName("should return false when customer data does not exist")
    void givenNonExistentCustomer_whenHasCustomerData_thenReturnFalse() {
      // Arrange
      given(customerRepository.existsByUserId(999L)).willReturn(false);

      // Act
      boolean result = customerService.hasCustomerData(999L);

      // Assert
      assertThat(result).isFalse();
    }
  }

  // =========================================================================
  // Tests for createOrUpdateCustomer() - Create scenario
  // =========================================================================
  @Nested
  @DisplayName("createOrUpdateCustomer() - Create New Customer")
  class CreateCustomerTests {

    @Test
    @DisplayName("should create customer when valid input and files provided")
    void givenValidInputWithFiles_whenCreateCustomer_thenSuccess() {
      // Arrange
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(customerRepository.findByUserId(1L)).willReturn(Optional.empty());
      given(customerRepository.findByNik(anyString())).willReturn(Optional.empty());
      given(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
          .willReturn("/uploads/test_file.jpg");
      given(customerRepository.save(any(Customer.class))).willReturn(testCustomer);
      given(emergencyContactRepository.findByCustomerId(anyLong())).willReturn(List.of());

      // Act
      CustomerResponse result =
          customerService.createOrUpdateCustomer(
              1L, testRequest, mockKtpFile, mockSelfieFile, null);

      // Assert
      assertThat(result).isNotNull();
      verify(customerRepository, times(1)).save(any(Customer.class));
      verify(fileStorageService, times(2)).storeFile(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("should throw exception when KTP file is missing for new customer")
    void givenMissingKtpFile_whenCreateCustomer_thenThrowValidationException() {
      // Arrange
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(customerRepository.findByUserId(1L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(
              () ->
                  customerService.createOrUpdateCustomer(
                      1L, testRequest, null, mockSelfieFile, null))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("KTP and Selfie photos are mandatory");
    }

    @Test
    @DisplayName("should throw exception when selfie file is missing for new customer")
    void givenMissingSelfieFile_whenCreateCustomer_thenThrowValidationException() {
      // Arrange
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(customerRepository.findByUserId(1L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(
              () ->
                  customerService.createOrUpdateCustomer(1L, testRequest, mockKtpFile, null, null))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("KTP and Selfie photos are mandatory");
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void givenNonExistentUser_whenCreateCustomer_thenThrowResourceNotFoundException() {
      // Arrange
      given(userRepository.findById(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(
              () ->
                  customerService.createOrUpdateCustomer(
                      999L, testRequest, mockKtpFile, mockSelfieFile, null))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User not found");
    }
  }

  // =========================================================================
  // Tests for createOrUpdateCustomer() - Update scenario
  // =========================================================================
  @Nested
  @DisplayName("createOrUpdateCustomer() - Update Existing Customer")
  class UpdateCustomerTests {

    @Test
    @DisplayName("should update customer when valid input provided without new files")
    void givenExistingCustomer_whenUpdateWithoutNewFiles_thenSuccess() {
      // Arrange
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(customerRepository.findByUserId(1L)).willReturn(Optional.of(testCustomer));
      given(customerRepository.findByNik(anyString())).willReturn(Optional.of(testCustomer));
      given(customerRepository.save(any(Customer.class))).willReturn(testCustomer);
      given(emergencyContactRepository.findByCustomerId(anyLong())).willReturn(List.of());

      // Act
      CustomerResponse result =
          customerService.createOrUpdateCustomer(1L, testRequest, null, null, null);

      // Assert
      assertThat(result).isNotNull();
      verify(customerRepository, times(1)).save(any(Customer.class));
      // No file storage calls since no new files
      verify(fileStorageService, never()).storeFile(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("should throw ValidationException when NIK belongs to another user")
    void givenDuplicateNik_whenUpdateCustomer_thenThrowValidationException() {
      // Arrange
      Customer anotherCustomer = new Customer();
      anotherCustomer.setId(2L);
      anotherCustomer.setUserId(2L);

      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(customerRepository.findByUserId(1L)).willReturn(Optional.of(testCustomer));
      given(customerRepository.findByNik(testRequest.getNik()))
          .willReturn(Optional.of(anotherCustomer));

      // Act & Assert
      assertThatThrownBy(
              () -> customerService.createOrUpdateCustomer(1L, testRequest, null, null, null))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("NIK")
          .hasMessageContaining("already registered");
    }
  }

  // =========================================================================
  // Tests for Emergency Contact handling
  // =========================================================================
  @Nested
  @DisplayName("Emergency Contact Handling")
  class EmergencyContactTests {

    @Test
    @DisplayName("should create emergency contact when provided in request")
    void givenEmergencyContact_whenCreateCustomer_thenSaveEmergencyContact() {
      // Arrange
      EmergencyContactRequest ecRequest = new EmergencyContactRequest();
      ecRequest.setName("Emergency Contact");
      ecRequest.setPhone("081234567899");
      ecRequest.setRelationship("Spouse");
      testRequest.setEmergencyContact(ecRequest);

      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(customerRepository.findByUserId(1L)).willReturn(Optional.empty());
      given(customerRepository.findByNik(anyString())).willReturn(Optional.empty());
      given(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
          .willReturn("/uploads/test_file.jpg");
      given(customerRepository.save(any(Customer.class))).willReturn(testCustomer);
      given(emergencyContactRepository.findByCustomerId(anyLong())).willReturn(List.of());

      // Act
      CustomerResponse result =
          customerService.createOrUpdateCustomer(
              1L, testRequest, mockKtpFile, mockSelfieFile, null);

      // Assert
      assertThat(result).isNotNull();
      verify(emergencyContactRepository, times(1)).deleteByCustomerId(anyLong());
      verify(emergencyContactRepository, times(1)).save(any(EmergencyContact.class));
    }

    @Test
    @DisplayName("should return customer with emergency contacts when they exist")
    void givenCustomerWithEmergencyContacts_whenGetCustomer_thenIncludeContacts() {
      // Arrange
      EmergencyContact ec = new EmergencyContact();
      ec.setId(1L);
      ec.setCustomerId(1L);
      ec.setContactName("Emergency Person");
      ec.setContactPhone("081234567899");
      ec.setRelationship("Spouse");
      ec.setCreatedAt(LocalDateTime.now());

      given(customerRepository.findById(1L)).willReturn(Optional.of(testCustomer));
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
      given(emergencyContactRepository.findByCustomerId(1L)).willReturn(List.of(ec));

      // Act
      CustomerResponse result = customerService.getCustomerById(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getEmergencyContacts()).hasSize(1);
      assertThat(result.getEmergencyContacts().get(0).getContactName())
          .isEqualTo("Emergency Person");
    }
  }
}
