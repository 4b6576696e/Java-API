package com.API.JWT.security.service;

import com.API.JWT.advice.InvalidRefreshToken;
import com.API.JWT.model.RefreshToken;
import com.API.JWT.repository.RefreshTokenRepo;
import com.API.JWT.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private UserRepo userRepo;

    @Value("${jwt.refreshToken.expirationTimeMs}")
    private Long refreshTokenDurationMs;

    public RefreshToken generateRefreshToken(Long id) {
        Integer userId = id.intValue();

        RefreshToken rt = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expirationAt(Instant.now().plusMillis(refreshTokenDurationMs))
                .user(userRepo.findById(userId).get())
                .build();

        refreshTokenRepo.save(rt);

        return rt;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public boolean isValidToken(RefreshToken refreshToken) {
        if (refreshToken.getExpirationAt().compareTo(Instant.now()) < 0) {
            refreshTokenRepo.delete((refreshToken));
            throw new InvalidRefreshToken("invalid refresh token");

        }

        return true;
    }
}
