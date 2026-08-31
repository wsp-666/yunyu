package com.yunyu.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${yunyu.jwt.secret}")
    private String secret;

    @Value("${yunyu.jwt.expire}")
    private long expire;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Integer userId, String account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);
        return buildToken(claims);
    }

    public String generateAdminToken(Integer adminId, String account, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", adminId);
        claims.put("account", account);
        claims.put("adminRole", role);
        return buildToken(claims);
    }

    private String buildToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer getUserId(String token) {
        return parseToken(token).get("userId", Integer.class);
    }

    public Integer getAdminRole(String token) {
        return parseToken(token).get("adminRole", Integer.class);
    }

    public boolean isAdmin(String token) {
        return getAdminRole(token) != null;
    }

    public boolean isSuperAdmin(String token) {
        Integer role = getAdminRole(token);
        return role != null && role == 1;
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
