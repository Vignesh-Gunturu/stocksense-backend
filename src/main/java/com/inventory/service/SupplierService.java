package com.inventory.service;

import com.inventory.dto.request.SupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Business;
import com.inventory.entity.Supplier;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final BusinessRepository businessRepository;

    public SupplierResponse create(Long businessId, SupplierRequest req) {
        Business business = businessRepository.getReferenceById(businessId);
        Supplier supplier = Supplier.builder()
                .name(req.getName())
                .company(req.getCompany())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .taxId(req.getTaxId())
                .leadTimeDays(req.getLeadTimeDays() != null ? req.getLeadTimeDays() : 7)
                .rating(req.getRating() != null ? req.getRating() : 0.0)
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .business(business)
                .build();
        return toResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> list(Long businessId) {
        return supplierRepository.findByBusinessId(businessId).stream().map(this::toResponse).toList();
    }

    public SupplierResponse update(Long businessId, Long id, SupplierRequest req) {
        Supplier s = getOwned(businessId, id);
        s.setName(req.getName());
        s.setCompany(req.getCompany());
        s.setEmail(req.getEmail());
        s.setPhone(req.getPhone());
        s.setAddress(req.getAddress());
        s.setTaxId(req.getTaxId());
        if (req.getLeadTimeDays() != null) s.setLeadTimeDays(req.getLeadTimeDays());
        if (req.getRating() != null) s.setRating(req.getRating());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
        return toResponse(supplierRepository.save(s));
    }

    public void delete(Long businessId, Long id) {
        supplierRepository.delete(getOwned(businessId, id));
    }

    private Supplier getOwned(Long businessId, Long id) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        if (!s.getBusiness().getId().equals(businessId)) throw new ResourceNotFoundException("Supplier not found");
        return s;
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(s.getId(), s.getName(), s.getCompany(), s.getEmail(), s.getPhone(),
                s.getAddress(), s.getTaxId(), s.getLeadTimeDays(), s.getRating(), s.getStatus());
    }
}
