package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBusinessId(Long businessId);
    boolean existsBySku(String sku);

    @Query("select p from Product p where p.business.id = :businessId and p.currentStock <= p.reorderPoint")
    List<Product> findLowStock(@Param("businessId") Long businessId);

    @Query("select p from Product p where p.business.id = :businessId and p.currentStock = 0")
    List<Product> findOutOfStock(@Param("businessId") Long businessId);

    @Query("select p from Product p where p.business.id = :businessId and p.currentStock > p.maxStock")
    List<Product> findOverstock(@Param("businessId") Long businessId);
}
