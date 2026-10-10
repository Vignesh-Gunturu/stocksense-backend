package com.inventory.repository;

import com.inventory.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByBusinessIdOrderBySaleDateDesc(Long businessId);
    List<Sale> findByBusinessIdAndSaleDateBetween(Long businessId, LocalDateTime start, LocalDateTime end);
}
