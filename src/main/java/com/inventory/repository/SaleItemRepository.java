package com.inventory.repository;

import com.inventory.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("select si from SaleItem si where si.product.id = :productId and si.sale.saleDate between :start and :end")
    List<SaleItem> findByProductAndDateRange(@Param("productId") Long productId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("select si from SaleItem si where si.sale.business.id = :businessId and si.sale.saleDate between :start and :end")
    List<SaleItem> findByBusinessAndDateRange(@Param("businessId") Long businessId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}
