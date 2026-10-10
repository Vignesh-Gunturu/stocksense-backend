package com.inventory.repository;

import com.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);
}
