package com.inventory.service;

import com.inventory.dto.request.StockAdjustmentRequest;
import com.inventory.dto.response.InventoryTransactionResponse;
import com.inventory.entity.Business;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.entity.User;
import com.inventory.exception.BadRequestException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional
    public InventoryTransactionResponse adjustStock(Long businessId, Long userId, StockAdjustmentRequest req) {
        Product product = productService.getOwned(businessId, req.getProductId());
        Business business = businessRepository.getReferenceById(businessId);
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        int delta = switch (req.getType()) {
            case STOCK_IN, RETURNED -> req.getQuantity();
            case STOCK_OUT, DAMAGED, LOST -> -req.getQuantity();
            case ADJUSTMENT -> req.getQuantity(); // can be positive or negative, caller decides sign
        };

        int newStock = product.getCurrentStock() + delta;
        if (newStock < 0) {
            throw new BadRequestException("Insufficient stock for this operation");
        }
        product.setCurrentStock(newStock);
        productRepository.save(product);

        InventoryTransaction txn = InventoryTransaction.builder()
                .product(product)
                .type(req.getType())
                .quantity(req.getQuantity())
                .reason(req.getReason())
                .user(user)
                .business(business)
                .build();
        txn = transactionRepository.save(txn);
        return toResponse(txn);
    }

    public List<InventoryTransactionResponse> history(Long businessId) {
        return transactionRepository.findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream().map(this::toResponse).toList();
    }

    public List<InventoryTransactionResponse> historyForProduct(Long productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).toList();
    }

    private InventoryTransactionResponse toResponse(InventoryTransaction t) {
        return new InventoryTransactionResponse(
                t.getId(),
                t.getProduct().getId(),
                t.getProduct().getName(),
                t.getType().name(),
                t.getQuantity(),
                t.getReason(),
                t.getUser() != null ? t.getUser().getName() : "System",
                t.getCreatedAt()
        );
    }
}
