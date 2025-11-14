package edu.dosw.application.services;

import edu.dosw.application.dto.AuthResponseDTO;
import edu.dosw.application.dto.LoginRequestDTO;
import edu.dosw.application.dto.TokenValidationDTO;
import edu.dosw.application.dto.UserCredentialsResponseDTO;
import edu.dosw.application.ports.AuthenticationUseCase;
import edu.dosw.application.ports.TokenUseCase;
import edu.dosw.application.ports.UserInfoUseCase;
import edu.dosw.domain.model.ValueObject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase {

    private final UserInfoUseCase userInfoUseCase;
    private final TokenUseCase tokenUseCase;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        String email = loginRequest.email().toLowerCase();

        if (!email.matches("^[\\w+.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            log.error("Invalid email format: {}", email);
            throw new RuntimeException("Invalid email format");
        }
        UserCredentialsResponseDTO credentials = userInfoUseCase.getUserCredentials(new Email(email));

        if (!passwordEncoder.matches(loginRequest.password(), credentials.password())) {
            log.error("Invalid password for email: {}", email);
            throw new RuntimeException("Invalid credentials");
        }

        String token = tokenUseCase.generateToken(
                credentials.userId(),
                credentials.email(),
                credentials.role().name()
        );

        log.info("User {} with role {} logged in successfully", credentials.email(), credentials.role());

        return new AuthResponseDTO(
                token,
                credentials.userId(),
                credentials.email(),
                credentials.role()
        );
    }

    @Override
    public TokenValidationDTO validateToken(String token) {
        boolean isValid = tokenUseCase.validateToken(token);
        if (!isValid) {
            return new TokenValidationDTO(false, null, null, null);
        }

        String email = tokenUseCase.extractUsername(token);
        String userId = tokenUseCase.extractUserId(token);
        String role = tokenUseCase.extractRole(token);

        return new TokenValidationDTO(true, email, userId, role);
    }

    @Override
    public String extractUsername(String token) {
        return tokenUseCase.extractUsername(token);
    }
}