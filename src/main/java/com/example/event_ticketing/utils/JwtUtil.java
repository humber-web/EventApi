package com.example.event_ticketing.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey SECRET_KEY;
    private final int EXPIRATION_TIME = 3600000; // 1 hour in milliseconds
    private static final String KEY_FILE = "jwt-secret.key";

    public JwtUtil() throws IOException {
        Path keyPath = Paths.get(KEY_FILE);

        if (Files.exists(keyPath)) {
            // Load existing key
            String secretKey = Files.readString(keyPath).trim();
            this.SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        } else {
            // Generate and save a new key
            this.SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            String encodedKey = Base64.getEncoder().encodeToString(this.SECRET_KEY.getEncoded());
            Files.write(keyPath, encodedKey.getBytes());
        }
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }
    
}
