package com.campus.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
@Slf4j
public class JwtUtil {


    private  String secret;
    private Integer expireHours;
    private SecretKey getSigningKey() {
        // 建议固定 UTF-8，避免平台默认编码差异
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(Long userId, String username){

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireHours * 3600L * 1000);


        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

   }

    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.info("Failed to parse token: " + e.getMessage());

            return null;
        }
    }
}