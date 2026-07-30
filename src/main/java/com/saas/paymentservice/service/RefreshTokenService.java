package com.saas.paymentservice.service;

import com.saas.paymentservice.entity.RefreshToken;
import com.saas.paymentservice.repository.RefreshTokenRepository;
import com.saas.paymentservice.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        String token = jwtService.generateRefreshToken();
        Instant expiresAt = Instant.now()
                .plusMillis(jwtService.getRefreshTokenExpirationMs());
        return refreshTokenRepository.save(new RefreshToken(token, userId, expiresAt));
    }

    @Transactional
    public RefreshToken verifyAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException(
                    refreshToken.isRevoked() ? "Refresh token revoked" : "Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
