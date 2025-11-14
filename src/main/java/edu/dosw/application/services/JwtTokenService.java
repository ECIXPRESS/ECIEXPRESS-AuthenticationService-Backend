package edu.dosw.application.services;

import edu.dosw.application.ports.TokenUseCase;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class JwtTokenService implements TokenUseCase {

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtTokenService(
            @Value("${jwt.secret:mySuperSecretKeyForJWTTokenGenerationInAuthenticationService}") String secret,
            @Value("${jwt.expiration:86400000}") long jwtExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    public String generateToken(String userId, String email, String role) {
        // El token JWT incluye userId, email y role para autorización en otros microservicios
        String token = Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)  // Incluir el rol en el token para autorización
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        log.debug("Generated JWT token for user: {} with role: {} (expires in {} ms)", email, role, jwtExpiration);
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    @Override
    public String extractRole(String token) {
        // Extraer el rol del token para verificación de autorización en otros servicios
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}