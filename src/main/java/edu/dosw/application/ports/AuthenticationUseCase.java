package edu.dosw.application.ports;

import edu.dosw.application.dto.AuthResponseDTO;
import edu.dosw.application.dto.LoginRequestDTO;
import edu.dosw.application.dto.TokenValidationDTO;

public interface AuthenticationUseCase {
    AuthResponseDTO login(LoginRequestDTO loginRequest);
    TokenValidationDTO validateToken(String token);
    String extractUsername(String token);
}