package com.inventory.service;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.response.UserResponse;
import com.inventory.entity.Business;
import com.inventory.entity.User;
import com.inventory.exception.BadRequestException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(Long businessId, CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BadRequestException("Business not found"));

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .business(business)
                .active(true)
                .build();
        user = userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> listUsers(Long businessId) {
        return userRepository.findAll().stream()
                .filter(u -> u.getBusiness() != null && u.getBusiness().getId().equals(businessId))
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.isActive());
    }
}
