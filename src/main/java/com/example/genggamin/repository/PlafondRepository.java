package com.example.genggamin.repository;

import com.example.genggamin.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk Plafond
 * Menyediakan query untuk data yang tidak dihapus (soft delete)
 */
@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long> {

    /**
     * Find all plafonds yang tidak dihapus (isDeleted = false)
     */
    @Query("SELECT p FROM Plafond p WHERE p.isDeleted = false ORDER BY p.minIncome ASC")
    List<Plafond> findAllActive();

    /**
     * Find plafond by ID dan tidak dihapus
     */
    @Query("SELECT p FROM Plafond p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Plafond> findByIdActive(@Param("id") Long id);

    /**
     * Find all active plafonds (isActive = true dan isDeleted = false)
     */
    @Query("SELECT p FROM Plafond p WHERE p.isActive = true AND p.isDeleted = false ORDER BY p.minIncome ASC")
    List<Plafond> findAllActiveAndNotDeleted();

    /**
     * Find plafond by income range
     * Untuk matching dengan customer income
     */
    @Query("SELECT p FROM Plafond p WHERE p.minIncome <= :income AND p.isActive = true AND p.isDeleted = false ORDER BY p.minIncome ASC")
    List<Plafond> findByIncomeRange(@Param("income") BigDecimal income);

    /**
     * Find plafond by tenor
     */
    @Query("SELECT p FROM Plafond p WHERE p.tenorMonth = :tenor AND p.isActive = true AND p.isDeleted = false")
    List<Plafond> findByTenor(@Param("tenor") Long tenor);

    /**
     * Check if duplicate exists (same minIncome, maxAmount, and tenor)
     * Untuk validasi saat create
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Plafond p WHERE p.minIncome = :minIncome AND p.maxAmount = :maxAmount AND p.tenorMonth = :tenor AND p.isDeleted = false")
    boolean existsByMinIncomeAndMaxAmountAndTenor(
        @Param("minIncome") BigDecimal minIncome,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("tenor") Long tenor
    );

    /**
     * Check if duplicate exists excluding current id
     * Untuk validasi saat update
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Plafond p WHERE p.minIncome = :minIncome AND p.maxAmount = :maxAmount AND p.tenorMonth = :tenor AND p.id != :id AND p.isDeleted = false")
    boolean existsByMinIncomeAndMaxAmountAndTenorAndIdNot(
        @Param("minIncome") BigDecimal minIncome,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("tenor") Long tenor,
        @Param("id") Long id
    );
}
