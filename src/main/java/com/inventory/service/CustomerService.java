package com.inventory.service;

import com.inventory.dto.request.CustomerRequest;
import com.inventory.dto.response.CustomerResponse;
import com.inventory.entity.Business;
import com.inventory.entity.Customer;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;

    public CustomerResponse create(Long businessId, CustomerRequest req) {
        Business business = businessRepository.getReferenceById(businessId);
        Customer customer = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .customerType(req.getCustomerType() != null ? req.getCustomerType() : "REGULAR")
                .business(business)
                .build();
        return toResponse(customerRepository.save(customer));
    }

    public List<CustomerResponse> list(Long businessId) {
        return customerRepository.findByBusinessId(businessId).stream().map(this::toResponse).toList();
    }

    public CustomerResponse update(Long businessId, Long id, CustomerRequest req) {
        Customer c = getOwned(businessId, id);
        c.setName(req.getName());
        c.setEmail(req.getEmail());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        if (req.getCustomerType() != null) c.setCustomerType(req.getCustomerType());
        return toResponse(customerRepository.save(c));
    }

    public void delete(Long businessId, Long id) {
        customerRepository.delete(getOwned(businessId, id));
    }

    private Customer getOwned(Long businessId, Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!c.getBusiness().getId().equals(businessId)) throw new ResourceNotFoundException("Customer not found");
        return c;
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress(), c.getCustomerType());
    }
}
