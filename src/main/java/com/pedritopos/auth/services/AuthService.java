package com.pedritopos.auth.services;

import javax.management.RuntimeErrorException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pedritopos.auth.domain.User;
import com.pedritopos.auth.dto.request.LoginRequest;
import com.pedritopos.auth.dto.response.LoginResponse;
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
}
