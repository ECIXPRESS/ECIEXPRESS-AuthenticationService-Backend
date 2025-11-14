package edu.dosw.application.ports;

public interface TokenUseCase {
    String generateToken(String userId, String email, String role);
    boolean validateToken(String token);
    String extractUsername(String token);
    String extractUserId(String token);
    String extractRole(String token);
}