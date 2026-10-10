package com.inventory.repository;

import com.inventory.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
