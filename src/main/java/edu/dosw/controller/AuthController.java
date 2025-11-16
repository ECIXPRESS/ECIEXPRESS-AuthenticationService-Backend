package edu.dosw.controller;

import edu.dosw.dto.AuthResponseDto;
import edu.dosw.dto.LogInDTO;
import edu.dosw.dto.TokenValidationResponse;
import edu.dosw.dto.UserInfoDto;
import edu.dosw.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Log in", security = {})
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LogInDTO logInDTO) {
        AuthResponseDto response = authenticationService.logIn(logInDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam String token) {
        boolean isValid = authenticationService.validateToken(token);
        return ResponseEntity.ok(new TokenValidationResponse(isValid));
    }

    @GetMapping("/extract-username")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Extract username from token")
    public ResponseEntity<Map<String, String>> extractUsername(@RequestParam String token) {
        String email = authenticationService.extractUsernameFromToken(token);
        return ResponseEntity.ok(Map.of("email", email));
    }

    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Refresh token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponseDto response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user info")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserInfoDto userInfo = authenticationService.getUserInfo(email);
        return ResponseEntity.ok(userInfo);
    }
}