package edu.dosw.application.services;


import edu.dosw.application.dto.UserCredentialsResponseDTO;
import edu.dosw.application.dto.UserInfoDTO;
import edu.dosw.application.ports.UserInfoUseCase;
import edu.dosw.domain.model.ValueObject.Email;
import edu.dosw.domain.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService implements UserInfoUseCase {

    private final RestTemplate restTemplate;
    private final String USER_SERVICE_BASE_URL = "http://localhost:8081/api/users";

    @Override
    public UserCredentialsResponseDTO getUserCredentials(Email email) {
        try {
            String url = USER_SERVICE_BASE_URL + "/credentials/" + email.value();
            log.debug("Calling user service via Gateway 8081: {}", url);

            ResponseEntity<UserCredentialsResponseDTO> response =
                    restTemplate.getForEntity(url, UserCredentialsResponseDTO.class);

            log.debug("Received credentials for email: {}", email.value());
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("User not found with email: {}", email.value());
            throw new RuntimeException("User not found");
        } catch (Exception e) {
            log.error("Error fetching user credentials for email: {} - Error: {}", email.value(), e.getMessage());
            throw new RuntimeException("User service unavailable via Gateway");
        }
    }

    @Override
    public UserInfoDTO getCurrentUserInfo(String email) {
        UserCredentialsResponseDTO credentials = getUserCredentials(new Email(email));
        return new UserInfoDTO(
                credentials.userId(),
                credentials.email(),
                credentials.role().name()
        );
    }

    @Override
    public boolean canAccessUserData(String currentUserEmail, String targetUserId) {
        UserInfoDTO currentUser = getCurrentUserInfo(currentUserEmail);

        if (currentUser.role().equals(Role.STUDENT.name()) ||
                currentUser.role().equals(Role.PROFESSOR.name())) {
            return currentUser.userId().equals(targetUserId);
        }

        return true;
    }

    @Override
    public boolean canAccessStudentData(String currentUserEmail, String studentId) {
        UserInfoDTO currentUser = getCurrentUserInfo(currentUserEmail);

        if (currentUser.role().equals(Role.STUDENT.name())) {
            return currentUser.userId().equals(studentId);
        }

        return true;
    }
}