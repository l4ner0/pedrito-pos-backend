package com.pedritopos.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pedritopos.auth.domain.RefreshToken;
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
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String accessToken = jwtService.generateToken(user.getId(), user.getBusinessId(), user.getRole().name());

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(user.getUsername(), accessToken, refreshToken.getToken(), user.getFullName(),
                user.getRole().name());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("El usuario ya está registrado");
        }

        User user = new User();
        user.setBusinessId(request.businessId());
        user.setFullName(request.fullName());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.valueOf(request.role().toUpperCase()));

        userRepository.save(user);

        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());
    }

    public LoginResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenService.validate(refreshToken);
        User user = token.getUser();

        String newAccessToken = jwtService.generateToken(user.getId(), user.getBusinessId(), user.getRole().name());

        RefreshToken newRefreshToken = refreshTokenService.create(user);

        return new LoginResponse(user.getUsername(), newAccessToken, newRefreshToken.getToken(), user.getFullName(),
                user.getRole().name());
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenService.validate(refreshToken);
        refreshTokenService.revokeAll(token.getUser().getId());
    }

}
