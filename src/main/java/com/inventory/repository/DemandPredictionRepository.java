package com.inventory.repository;

import com.inventory.entity.DemandPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DemandPredictionRepository extends JpaRepository<DemandPrediction, Long> {
    List<DemandPrediction> findByBusinessIdOrderByGeneratedAtDesc(Long businessId);
    Optional<DemandPrediction> findFirstByProductIdOrderByGeneratedAtDesc(Long productId);
    void deleteByProductId(Long productId);
}
