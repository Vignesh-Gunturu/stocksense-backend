package com.inventory.service;

import com.inventory.dto.request.ProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Business;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.BadRequestException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductResponse create(Long businessId, ProductRequest req) {
        if (productRepository.existsBySku(req.getSku())) {
            throw new BadRequestException("A product with this SKU already exists");
        }
        Business business = businessRepository.getReferenceById(businessId);
        Category category = req.getCategoryId() != null ? categoryRepository.findById(req.getCategoryId()).orElse(null) : null;
        Supplier supplier = req.getSupplierId() != null ? supplierRepository.findById(req.getSupplierId()).orElse(null) : null;

        Product product = Product.builder()
                .sku(req.getSku())
                .barcode(req.getBarcode())
                .name(req.getName())
                .description(req.getDescription())
                .category(category)
                .supplier(supplier)
                .unit(req.getUnit() != null ? req.getUnit() : "pcs")
                .purchasePrice(req.getPurchasePrice())
                .sellingPrice(req.getSellingPrice())
                .tax(req.getTax() != null ? req.getTax() : 0.0)
                .minStock(req.getMinStock() != null ? req.getMinStock() : 0)
                .maxStock(req.getMaxStock() != null ? req.getMaxStock() : 1000)
                .reorderPoint(req.getReorderPoint() != null ? req.getReorderPoint() : 10)
                .leadTimeDays(req.getLeadTimeDays() != null ? req.getLeadTimeDays() : 7)
                .currentStock(req.getCurrentStock() != null ? req.getCurrentStock() : 0)
                .imageUrl(req.getImageUrl())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .business(business)
                .build();

        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> list(Long businessId) {
        return productRepository.findByBusinessId(businessId).stream().map(this::toResponse).toList();
    }

    public ProductResponse get(Long businessId, Long id) {
        return toResponse(getOwned(businessId, id));
    }

    public ProductResponse update(Long businessId, Long id, ProductRequest req) {
        Product product = getOwned(businessId, id);
        Category category = req.getCategoryId() != null ? categoryRepository.findById(req.getCategoryId()).orElse(null) : null;
        Supplier supplier = req.getSupplierId() != null ? supplierRepository.findById(req.getSupplierId()).orElse(null) : null;

        product.setBarcode(req.getBarcode());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setCategory(category);
        product.setSupplier(supplier);
        if (req.getUnit() != null) product.setUnit(req.getUnit());
        product.setPurchasePrice(req.getPurchasePrice());
        product.setSellingPrice(req.getSellingPrice());
        if (req.getTax() != null) product.setTax(req.getTax());
        if (req.getMinStock() != null) product.setMinStock(req.getMinStock());
        if (req.getMaxStock() != null) product.setMaxStock(req.getMaxStock());
        if (req.getReorderPoint() != null) product.setReorderPoint(req.getReorderPoint());
        if (req.getLeadTimeDays() != null) product.setLeadTimeDays(req.getLeadTimeDays());
        product.setImageUrl(req.getImageUrl());
        if (req.getStatus() != null) product.setStatus(req.getStatus());

        return toResponse(productRepository.save(product));
    }

    public void delete(Long businessId, Long id) {
        productRepository.delete(getOwned(businessId, id));
    }

    public List<ProductResponse> lowStock(Long businessId) {
        return productRepository.findLowStock(businessId).stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> outOfStock(Long businessId) {
        return productRepository.findOutOfStock(businessId).stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> overstock(Long businessId) {
        return productRepository.findOverstock(businessId).stream().map(this::toResponse).toList();
    }

    Product getOwned(Long businessId, Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!p.getBusiness().getId().equals(businessId)) throw new ResourceNotFoundException("Product not found");
        return p;
    }

    public String stockStatus(Product p) {
        if (p.getCurrentStock() == 0) return "OUT_OF_STOCK";
        if (p.getCurrentStock() <= p.getReorderPoint()) return "LOW_STOCK";
        if (p.getCurrentStock() > p.getMaxStock()) return "OVERSTOCK";
        return "IN_STOCK";
    }

    ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getSku(), p.getBarcode(), p.getName(), p.getDescription(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getSupplier() != null ? p.getSupplier().getId() : null,
                p.getSupplier() != null ? p.getSupplier().getName() : null,
                p.getUnit(), p.getPurchasePrice(), p.getSellingPrice(), p.getTax(),
                p.getMinStock(), p.getMaxStock(), p.getReorderPoint(), p.getLeadTimeDays(),
                p.getCurrentStock(), p.getImageUrl(), p.getStatus(), stockStatus(p)
        );
    }
}
