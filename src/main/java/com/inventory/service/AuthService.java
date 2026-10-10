package com.inventory.service;

import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.RegisterBusinessRequest;
import com.inventory.dto.response.AuthResponse;
import com.inventory.entity.Business;
import com.inventory.entity.Role;
import com.inventory.entity.User;
import com.inventory.exception.BadRequestException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerBusiness(RegisterBusinessRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        Business business = Business.builder()
                .name(req.getBusinessName())
                .build();
        business = businessRepository.save(business);

        User admin = User.builder()
                .name(req.getAdminName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.BUSINESS_ADMIN)
                .business(business)
                .active(true)
                .build();
        admin = userRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getEmail(), admin.getId(), admin.getRole().name(), business.getId());
        return new AuthResponse(token, admin.getId(), admin.getName(), admin.getEmail(),
                admin.getRole().name(), business.getId(), business.getName());
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(),
                user.getBusiness() != null ? user.getBusiness().getId() : null);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
                user.getBusiness() != null ? user.getBusiness().getId() : null,
                user.getBusiness() != null ? user.getBusiness().getName() : null);
    }
}
