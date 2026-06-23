package com.pedritopos.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pedritopos.auth.domain.User;
import com.pedritopos.auth.dto.request.LoginRequest;
import com.pedritopos.auth.dto.request.RegisterRequest;
import com.pedritopos.auth.dto.response.LoginResponse;
import com.pedritopos.auth.dto.response.RegisterResponse;
import com.pedritopos.auth.enums.Role;
import com.pedritopos.auth.repositories.UserRepository;
import com.pedritopos.shared.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getId(), user.getBusinessId(), user.getRole().name());

        return new LoginResponse(token, user.getFullName(), user.getRole().name());
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = new User();
        user.setBusinessId(request.businessId());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.valueOf(request.role().toUpperCase()));

        userRepository.save(user);

        return new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());
    }

}
