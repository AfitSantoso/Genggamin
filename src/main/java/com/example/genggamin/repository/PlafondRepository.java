package com.example.genggamin.repository;

import com.example.genggamin.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository untuk Plafond
 */
@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long> {

    /**
     * Find all plafonds sorted by minIncome
     */
    @Query("SELECT p FROM Plafond p ORDER BY p.minIncome ASC")
    List<Plafond> findAllSorted();

    /**
     * Find all active plafonds (isActive = true)
     */
    @Query("SELECT p FROM Plafond p WHERE p.isActive = true ORDER BY p.minIncome ASC")
    List<Plafond> findAllActive();

    /**
     * Find plafond by income range
     * Untuk matching dengan customer income
     */
    @Query("SELECT p FROM Plafond p WHERE p.minIncome <= :income AND p.isActive = true ORDER BY p.minIncome ASC")
    List<Plafond> findByIncomeRange(@Param("income") BigDecimal income);

    /**
     * Find plafond by tenor
     */
    @Query("SELECT p FROM Plafond p WHERE p.tenorMonth = :tenor AND p.isActive = true")
    List<Plafond> findByTenor(@Param("tenor") Long tenor);

    /**
     * Check if duplicate exists (same minIncome, maxAmount, and tenor)
     * Untuk validasi saat create
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Plafond p WHERE p.minIncome = :minIncome AND p.maxAmount = :maxAmount AND p.tenorMonth = :tenor")
    boolean existsByMinIncomeAndMaxAmountAndTenor(
        @Param("minIncome") BigDecimal minIncome,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("tenor") Long tenor
    );

    /**
     * Check if duplicate exists excluding current id
     * Untuk validasi saat update
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Plafond p WHERE p.minIncome = :minIncome AND p.maxAmount = :maxAmount AND p.tenorMonth = :tenor AND p.id != :id")
    boolean existsByMinIncomeAndMaxAmountAndTenorAndIdNot(
        @Param("minIncome") BigDecimal minIncome,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("tenor") Long tenor,
        @Param("id") Long id
    );
}
