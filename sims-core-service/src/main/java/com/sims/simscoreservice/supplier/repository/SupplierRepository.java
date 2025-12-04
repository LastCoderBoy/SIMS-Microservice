package com.sims.simscoreservice.supplier.repository;

import com.sims.simscoreservice.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Supplier Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) = LOWER(:name)")
    Optional<Supplier> findByName(@Param("name") String name);

    @Query("SELECT s FROM Supplier s WHERE LOWER(s.email) = LOWER(:email)")
    Optional<Supplier> findByEmail(@Param("email") String email);

    /**
     * Check if supplier exists by name (excluding specific ID)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supplier s WHERE LOWER(s.name) = LOWER(:name) AND s.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Check if supplier exists by email (excluding specific ID)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supplier s WHERE LOWER(s.email) = LOWER(:email) AND s.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}
