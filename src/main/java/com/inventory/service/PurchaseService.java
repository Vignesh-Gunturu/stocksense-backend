package com.inventory.service;

import com.inventory.dto.request.PurchaseOrderRequest;
import com.inventory.dto.request.ReceivePurchaseRequest;
import com.inventory.dto.response.PurchaseOrderResponse;
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
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final BusinessRepository businessRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public PurchaseOrderResponse create(Long businessId, PurchaseOrderRequest req) {
        Business business = businessRepository.getReferenceById(businessId);
        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .expectedDelivery(req.getExpectedDelivery())
                .business(business)
                .status(PurchaseOrder.PurchaseStatus.ORDERED)
                .build();

        double total = 0.0;
        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));
            double price = itemReq.getPurchasePrice() != null ? itemReq.getPurchasePrice() : product.getPurchasePrice();
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .quantityReceived(0)
                    .purchasePrice(price)
                    .build();
            po.getItems().add(item);
            total += price * itemReq.getQuantity();
        }
        po.setTotalAmount(total);
        po = purchaseOrderRepository.save(po);
        return toResponse(po);
    }

    public List<PurchaseOrderResponse> list(Long businessId) {
        return purchaseOrderRepository.findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream().map(this::toResponse).toList();
    }

    public PurchaseOrderResponse get(Long businessId, Long id) {
        return toResponse(getOwned(businessId, id));
    }

    @Transactional
    public PurchaseOrderResponse receive(Long businessId, Long userId, Long poId, ReceivePurchaseRequest req) {
        PurchaseOrder po = getOwned(businessId, poId);
        Business business = businessRepository.getReferenceById(businessId);
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        if (po.getStatus() == PurchaseOrder.PurchaseStatus.RECEIVED) {
            throw new BadRequestException("Purchase order already fully received");
        }

        for (var receiveItem : req.getItems()) {
            PurchaseOrderItem item = po.getItems().stream()
                    .filter(i -> i.getId().equals(receiveItem.getPurchaseOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase order item not found"));

            int remaining = item.getQuantity() - item.getQuantityReceived();
            int receiving = Math.min(receiveItem.getQuantityReceived(), remaining);
            if (receiving <= 0) continue;

            item.setQuantityReceived(item.getQuantityReceived() + receiving);

            Product product = item.getProduct();
            product.setCurrentStock(product.getCurrentStock() + receiving);
            productRepository.save(product);

            InventoryTransaction txn = InventoryTransaction.builder()
                    .product(product)
                    .type(InventoryTransaction.MovementType.STOCK_IN)
                    .quantity(receiving)
                    .reason("Purchase order #" + po.getId() + " received")
                    .reference("PO-" + po.getId())
                    .user(user)
                    .business(business)
                    .build();
            transactionRepository.save(txn);
        }

        boolean allReceived = po.getItems().stream()
                .allMatch(i -> i.getQuantityReceived().intValue() >= i.getQuantity().intValue());
        boolean anyReceived = po.getItems().stream().anyMatch(i -> i.getQuantityReceived() > 0);
        po.setStatus(allReceived ? PurchaseOrder.PurchaseStatus.RECEIVED
                : anyReceived ? PurchaseOrder.PurchaseStatus.PARTIALLY_RECEIVED
                : po.getStatus());

        return toResponse(purchaseOrderRepository.save(po));
    }

    public void cancel(Long businessId, Long id) {
        PurchaseOrder po = getOwned(businessId, id);
        if (po.getStatus() == PurchaseOrder.PurchaseStatus.RECEIVED
                || po.getStatus() == PurchaseOrder.PurchaseStatus.PARTIALLY_RECEIVED) {
            throw new BadRequestException("Cannot cancel a purchase order that has received stock");
        }
        po.setStatus(PurchaseOrder.PurchaseStatus.CANCELLED);
        purchaseOrderRepository.save(po);
    }

    private PurchaseOrder getOwned(Long businessId, Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if (!po.getBusiness().getId().equals(businessId)) throw new ResourceNotFoundException("Purchase order not found");
        return po;
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        List<PurchaseOrderResponse.Item> items = po.getItems().stream()
                .map(i -> new PurchaseOrderResponse.Item(
                        i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getQuantityReceived(), i.getPurchasePrice()))
                .toList();
        return new PurchaseOrderResponse(
                po.getId(), po.getSupplier().getId(), po.getSupplier().getName(),
                po.getStatus().name(), po.getOrderDate(), po.getExpectedDelivery(),
                po.getTotalAmount(), items, po.getCreatedAt());
    }
}
