package edu.dosw.infrastructure.web;


import edu.dosw.application.dto.AuthResponseDTO;
import edu.dosw.application.dto.LoginRequestDTO;
import edu.dosw.application.dto.TokenValidationDTO;
import edu.dosw.application.ports.AuthenticationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthenticationController {

    private final AuthenticationUseCase authenticationUseCase;

    @PostMapping("/login")
    @Operation(summary = "Log in user")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        AuthResponseDTO response = authenticationUseCase.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<TokenValidationDTO> validateToken(@RequestParam String token) {
        TokenValidationDTO validation = authenticationUseCase.validateToken(token);
        return ResponseEntity.ok(validation);
    }

    @GetMapping("/extract-username")
    @Operation(summary = "Extract username from token")
    public ResponseEntity<String> extractUsername(@RequestParam String token) {
        String username = authenticationUseCase.extractUsername(token);
        return ResponseEntity.ok(username);
    }
}