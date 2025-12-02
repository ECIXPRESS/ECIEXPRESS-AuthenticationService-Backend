package edu.dosw.services;

import edu.dosw.dto.UserCredentialsDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    @Value("${gateway.url:https://api-gateway-despliegue.onrender.com/api}")
    private String gatewayBaseUrl;

    public Optional<UserCredentialsDto> getUserByEmail(String email) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(gatewayBaseUrl)
                    .path("/users/credentials/{email}")
                    .buildAndExpand(email)
                    .toUriString();

            logger.info("Calling users-service via gateway: {}", url);

            ResponseEntity<UserCredentialsDto> response = restTemplate.getForEntity(url, UserCredentialsDto.class);
            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("User not found with email: {}", email);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to gateway: {} - URL: {}", e.getMessage(), gatewayBaseUrl);
            throw new RuntimeException("Cannot connect to API Gateway: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error contacting user service via gateway: {}", e.getMessage());
            throw new RuntimeException("Error contacting user service via gateway: " + e.getMessage(), e);
        }
    }

    public Optional<UserCredentialsDto> validateCredentials(String email, String password) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(gatewayBaseUrl)
                    .path("/users/credentials/auth")
                    .queryParam("email", email)
                    .queryParam("password", password)
                    .toUriString();

            logger.info("Validating credentials via gateway: {}", url.replace(password, "***"));

            ResponseEntity<UserCredentialsDto> response = restTemplate.getForEntity(url, UserCredentialsDto.class);
            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.Unauthorized e) {
            logger.warn("Invalid credentials for email: {}", email);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to gateway for validation: {} - URL: {}", e.getMessage(), gatewayBaseUrl);
            throw new RuntimeException("Cannot connect to API Gateway: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error validating credentials via gateway: {}", e.getMessage());
            throw new RuntimeException("Error validating credentials via gateway: " + e.getMessage(), e);
        }
    }

    public Optional<UserCredentialsDto> getUserById(String userId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(gatewayBaseUrl)
                    .path("/users/{userId}/credentials")
                    .buildAndExpand(userId)
                    .toUriString();

            logger.info("Getting user by ID via gateway: {}", url);

            ResponseEntity<UserCredentialsDto> response = restTemplate.getForEntity(url, UserCredentialsDto.class);
            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("User not found with ID: {}", userId);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to gateway: {} - URL: {}", e.getMessage(), gatewayBaseUrl);
            throw new RuntimeException("Cannot connect to API Gateway: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error getting user by ID via gateway: {}", e.getMessage());
            throw new RuntimeException("Error getting user by ID via gateway: " + e.getMessage(), e);
        }
    }
}