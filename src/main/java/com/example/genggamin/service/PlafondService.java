package com.example.genggamin.service;

import com.example.genggamin.dto.PlafondRequest;
import com.example.genggamin.dto.PlafondResponse;
import com.example.genggamin.entity.Plafond;
import com.example.genggamin.repository.PlafondRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk mengelola Plafond
 * Menggunakan Redis caching untuk performa optimal
 */
@Service
@Transactional
public class PlafondService {

    private final PlafondRepository plafondRepository;

    public PlafondService(PlafondRepository plafondRepository) {
        this.plafondRepository = plafondRepository;
    }

    /**
     * Get all plafonds
     * Cached dengan key "allPlafonds"
     */
    @Cacheable(value = "plafonds", key = "'allPlafonds'")
    @Transactional(readOnly = true)
    public List<PlafondResponse> getAllPlafonds() {
        return plafondRepository.findAllSorted().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active plafonds (isActive = true)
     * Cached dengan key "activePlafonds"
     */
    @Cacheable(value = "plafonds", key = "'activePlafonds'")
    @Transactional(readOnly = true)
    public List<PlafondResponse> getActivePlafonds() {
        return plafondRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get plafond by ID
     * Cached dengan key berdasarkan ID
     */
    @Cacheable(value = "plafonds", key = "'plafond:' + #id")
    @Transactional(readOnly = true)
    public PlafondResponse getPlafondById(Long id) {
        Plafond plafond = plafondRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));
        return mapToResponse(plafond);
    }

    /**
     * Get plafonds by income range
     * Untuk matching dengan customer income
     */
    @Cacheable(value = "plafonds", key = "'byIncome:' + #income")
    @Transactional(readOnly = true)
    public List<PlafondResponse> getPlafondsByIncome(BigDecimal income) {
        return plafondRepository.findByIncomeRange(income).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new plafond
     * Evict cache untuk refresh data
     * Hanya bisa dilakukan oleh ADMIN
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public PlafondResponse createPlafond(PlafondRequest request) {
        // Validasi request
        request.validate();

        // Check duplicate
        if (plafondRepository.existsByMinIncomeAndMaxAmountAndTenor(
                request.getMinIncome(), 
                request.getMaxAmount(), 
                request.getTenorMonth())) {
            throw new RuntimeException("Plafond with same min income, max amount, and tenor already exists");
        }

        // Build entity
        Plafond plafond = Plafond.builder()
                .minIncome(request.getMinIncome())
                .maxAmount(request.getMaxAmount())
                .tenorMonth(request.getTenorMonth())
                .interestRate(request.getInterestRate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Plafond saved = plafondRepository.saveAndFlush(plafond);
        return mapToResponse(saved);
    }

    /**
     * Update existing plafond
     * Evict cache untuk refresh data
     * Hanya bisa dilakukan oleh ADMIN
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public PlafondResponse updatePlafond(Long id, PlafondRequest request) {
        // Validasi request
        request.validate();

        // Find existing plafond
        Plafond plafond = plafondRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

        // Check duplicate (exclude current plafond)
        if (plafondRepository.existsByMinIncomeAndMaxAmountAndTenorAndIdNot(
                request.getMinIncome(), 
                request.getMaxAmount(), 
                request.getTenorMonth(), 
                id)) {
            throw new RuntimeException("Plafond with same min income, max amount, and tenor already exists");
        }

        // Update fields
        plafond.setMinIncome(request.getMinIncome());
        plafond.setMaxAmount(request.getMaxAmount());
        plafond.setTenorMonth(request.getTenorMonth());
        plafond.setInterestRate(request.getInterestRate());
        
        if (request.getIsActive() != null) {
            plafond.setIsActive(request.getIsActive());
        }

        Plafond updated = plafondRepository.saveAndFlush(plafond);
        return mapToResponse(updated);
    }

    /**
     * Delete plafond (hard delete)
     * Evict cache untuk refresh data
     * Hanya bisa dilakukan oleh ADMIN
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public void deletePlafond(Long id) {
        Plafond plafond = plafondRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

        plafondRepository.delete(plafond);
    }

    /**
     * Toggle plafond active status
     * Hanya bisa dilakukan oleh ADMIN
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public PlafondResponse toggleActiveStatus(Long id) {
        Plafond plafond = plafondRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

        plafond.setIsActive(!plafond.getIsActive());
        Plafond updated = plafondRepository.saveAndFlush(plafond);
        return mapToResponse(updated);
    }

    /**
     * Helper method untuk mapping Entity ke DTO
     */
    private PlafondResponse mapToResponse(Plafond plafond) {
        return PlafondResponse.builder()
                .id(plafond.getId())
                .minIncome(plafond.getMinIncome())
                .maxAmount(plafond.getMaxAmount())
                .tenorMonth(plafond.getTenorMonth())
                .interestRate(plafond.getInterestRate())
                .isActive(plafond.getIsActive())
                .build();
    }
}
