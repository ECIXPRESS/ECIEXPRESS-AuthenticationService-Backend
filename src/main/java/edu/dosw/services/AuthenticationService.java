package edu.dosw.services;

import edu.dosw.dto.AuthResponseDto;
import edu.dosw.dto.LogInDTO;
import edu.dosw.dto.UserCredentialsDto;
import edu.dosw.dto.UserInfoDto;
import edu.dosw.exception.AuthenticationException;
import edu.dosw.exception.ResourceNotFoundException;
import edu.dosw.model.enums.Role;
import edu.dosw.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final EventPublisherService eventPublisherService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponseDto logIn(LogInDTO logInDTO) {
        String email = logInDTO.email().toLowerCase();

        logger.info("=== LOGIN ATTEMPT STARTED ===");
        logger.info("Email: {}", email);
        logger.info("Connecting via API Gateway: https://api-gateway-despliegue.onrender.com");

        if (!isValidEmail(email)) {
            logger.error("Invalid email format: {}", email);
            throw new AuthenticationException("Invalid email format");
        }

        try {
            logger.info("Getting user by email via API Gateway: {}", email);
            Optional<UserCredentialsDto> userCredentials = userServiceClient.getUserByEmail(email);

            if (userCredentials.isEmpty()) {
                logger.error("User not found with email: {}", email);
                throw new AuthenticationException("Invalid credentials");
            }

            UserCredentialsDto user = userCredentials.get();
            logger.info("User found via Gateway: {} with role: {}", user.email(), user.role());
            logger.info("Stored password hash: {}", user.password());

            logger.info("Validating password...");
            boolean passwordMatches = passwordEncoder.matches(logInDTO.password(), user.password());
            logger.info("Password match result: {}", passwordMatches);

            if (!passwordMatches) {
                logger.error("Invalid password for email: {}", email);
                throw new AuthenticationException("Invalid credentials");
            }

            logger.info("Generating JWT tokens...");
            String token = jwtUtil.generateToken(user.userId(), user.email(), user.role());
            String refreshToken = jwtUtil.generateRefreshToken(user.userId(), user.email(), user.role());

            UserInfoDto userInfo = new UserInfoDto(user.userId(), user.email(), user.role(), user.pfpURL());

            try {
                String userName = extractNameFromEmail(user.email());
                eventPublisherService.publishLoginSuccess(
                        user.email(),
                        user.userId(),
                        userName,
                        getClientIp()
                );
                logger.info("Evento de login exitoso publicado para: {}", user.email());
            } catch (Exception e) {
                logger.error("Error publicando evento de login (login continua): {}", e.getMessage());
            }

            logger.info("=== LOGIN SUCCESSFUL ===");
            logger.info("User: {}, Role: {}, UserId: {}", user.email(), user.role(), user.userId());
            logger.info("Successfully authenticated via API Gateway");

            return new AuthResponseDto(token, refreshToken, userInfo, jwtUtil.getExpirationTime());

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Login failed with error: {}", e.getMessage(), e);
            throw new AuthenticationException("Login failed: " + e.getMessage());
        }
    }

    private String extractNameFromEmail(String email) {
        String namePart = email.split("@")[0];
        return namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
    }

    private String getClientIp() {
        return "190.85.250.100";
    }

    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsernameFromToken(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            throw new AuthenticationException("Invalid token");
        }
    }

    public AuthResponseDto refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String email = jwtUtil.extractUsername(refreshToken);
            String userId = jwtUtil.extractUserId(refreshToken);
            Role role = jwtUtil.extractRole(refreshToken);

            logger.info("Refreshing token for user via API Gateway: {}", email);
            Optional<UserCredentialsDto> userCredentials = userServiceClient.getUserByEmail(email);
            if (userCredentials.isEmpty()) {
                throw new AuthenticationException("User not found via API Gateway");
            }

            String newAccessToken = jwtUtil.generateToken(userId, email, role);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, email, role);

            UserCredentialsDto user = userCredentials.get();
            UserInfoDto userInfo = new UserInfoDto(user.userId(), user.email(), user.role(), user.pfpURL());

            return new AuthResponseDto(newAccessToken, newRefreshToken, userInfo, jwtUtil.getExpirationTime());

        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Token refresh failed");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public UserInfoDto getUserInfo(String email) {
        logger.info("Getting user info via API Gateway: {}", email);
        Optional<UserCredentialsDto> userCredentials = userServiceClient.getUserByEmail(email);
        if (userCredentials.isEmpty()) {
            throw new ResourceNotFoundException("User not found via API Gateway with email: " + email);
        }

        UserCredentialsDto user = userCredentials.get();
        return new UserInfoDto(user.userId(), user.email(), user.role(), user.pfpURL());
    }

    public Optional<UserCredentialsDto> getByEmail(String email) {
        logger.info("Getting user by email via API Gateway: {}", email);
        return userServiceClient.getUserByEmail(email);
    }

    public Optional<UserCredentialsDto> getByUserId(String id) {
        logger.info("Getting user by ID via API Gateway: {}", id);
        return userServiceClient.getUserById(id);
    }

    public boolean canAccessStudentData(Authentication authentication, String studentId) {
        String currentUserEmail = authentication.getName();
        UserInfoDto currentUser = getUserInfo(currentUserEmail);

        if (currentUser.role() == Role.STUDENT) {
            return currentUser.userId().equals(studentId);
        }

        return true;
    }

    public boolean canAccessUserData(Authentication authentication, String userId) {
        String currentUserEmail = authentication.getName();
        UserInfoDto currentUser = getUserInfo(currentUserEmail);

        if (currentUser.role() == Role.STUDENT || currentUser.role() == Role.PROFESSOR) {
            return currentUser.userId().equals(userId);
        }

        return true;
    }

    public String getCurrentUserId(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        UserInfoDto currentUser = getUserInfo(currentUserEmail);
        return currentUser.userId();
    }

    public Role getCurrentUserRole(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        UserInfoDto currentUser = getUserInfo(currentUserEmail);
        return currentUser.role();
    }
}