package com.API.JWT.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.util.WebUtils;


@Component
public class JwtUtils {

    @Value("${jwt.accessToken.expirationTimeMs}")
    private int jwtExpirationMs;

    @Value("${jwt.accessToken.signInKey}")
    private String jwtSignInKey;

    @Value("${jwt.accessToken.cookieName}")
    private String jwtCookieName;

    @Value("${jwt.refreshToken.cookieName}")
    private String jwtRefreshCookieName;

    public ResponseCookie generateJWTCookie(UserDetails user) {
        String token = generateToken(user.getUsername());
        return generateCookie(jwtCookieName, token, "/api");
    }

    private ResponseCookie generateCookie(String jwtCookieName, String token, String path) {
        return ResponseCookie.from(jwtCookieName, token)
                .path(path)
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();
    }

    private String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key signInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSignInKey));
    }

    public ResponseCookie generateJWTRefreshCookie(String token) {
        return generateCookie(jwtRefreshCookieName, token, "/api/auth/refreshToken");
    }

    public String extractJWTFromCookie(HttpServletRequest request) {
        return getUserNameFromCookie(request, jwtCookieName);
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        return getUserNameFromCookie(request, jwtRefreshCookieName);
    }

    private String getUserNameFromCookie(HttpServletRequest request, String jwtCookieName) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if (cookie == null) return null;
        else return cookie.getValue();
    }

    public boolean isValidToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(signInKey())
                .build()
                .parse(token);

        return true;
    }

    public String extractUserName(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
