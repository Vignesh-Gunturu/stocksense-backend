package com.inventory.service;

import com.inventory.dto.request.SaleRequest;
import com.inventory.dto.response.SaleResponse;
import com.inventory.entity.*;
import com.inventory.exception.BadRequestException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public SaleResponse create(Long businessId, Long userId, SaleRequest req) {
        Business business = businessRepository.getReferenceById(businessId);
        Customer customer = req.getCustomerId() != null
                ? customerRepository.findById(req.getCustomerId()).orElse(null) : null;
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        Sale sale = Sale.builder()
                .customer(customer)
                .discount(req.getDiscount() != null ? req.getDiscount() : 0.0)
                .tax(req.getTax() != null ? req.getTax() : 0.0)
                .paymentStatus(req.getPaymentStatus() != null ? req.getPaymentStatus() : "PAID")
                .business(business)
                .build();

        double subtotal = 0.0;
        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            if (product.getCurrentStock() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            double price = itemReq.getPrice() != null ? itemReq.getPrice() : product.getSellingPrice();
            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(price)
                    .build();
            sale.getItems().add(item);
            subtotal += price * itemReq.getQuantity();

            product.setCurrentStock(product.getCurrentStock() - itemReq.getQuantity());
            productRepository.save(product);

            InventoryTransaction txn = InventoryTransaction.builder()
                    .product(product)
                    .type(InventoryTransaction.MovementType.STOCK_OUT)
                    .quantity(itemReq.getQuantity())
                    .reason("Sale")
                    .user(user)
                    .business(business)
                    .build();
            transactionRepository.save(txn);
        }

        double total = subtotal - sale.getDiscount() + sale.getTax();
        sale.setTotalAmount(total);
        sale = saleRepository.save(sale);
        return toResponse(sale);
    }

    public List<SaleResponse> list(Long businessId) {
        return saleRepository.findByBusinessIdOrderBySaleDateDesc(businessId).stream().map(this::toResponse).toList();
    }

    public SaleResponse get(Long businessId, Long id) {
        Sale sale = saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (!sale.getBusiness().getId().equals(businessId)) throw new ResourceNotFoundException("Sale not found");
        return toResponse(sale);
    }

    private SaleResponse toResponse(Sale s) {
        List<SaleResponse.Item> items = s.getItems().stream()
                .map(i -> new SaleResponse.Item(i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getPrice()))
                .toList();
        return new SaleResponse(s.getId(), s.getCustomer() != null ? s.getCustomer().getId() : null,
                s.getCustomer() != null ? s.getCustomer().getName() : "Walk-in Customer",
                s.getSaleDate(), s.getDiscount(), s.getTax(), s.getTotalAmount(), s.getPaymentStatus(), items);
    }
}
