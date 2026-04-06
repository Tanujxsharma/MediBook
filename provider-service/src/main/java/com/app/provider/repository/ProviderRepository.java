package com.app.provider.repository;

import com.app.provider.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    List<Provider> findBySpecialization(String specialization);

    List<Provider> findByVerified(boolean verified);

    List<Provider> findByAvailable(boolean available);

    Optional<Provider> findByUserId(Long userId);

    int countBySpecialization(String specialization);

    List<Provider> findByClinicAddressContainingIgnoreCase(String location);

    @Query("SELECT p FROM Provider p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.specialization) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Provider> searchByNameOrSpecialization(@Param("keyword") String keyword);
}
