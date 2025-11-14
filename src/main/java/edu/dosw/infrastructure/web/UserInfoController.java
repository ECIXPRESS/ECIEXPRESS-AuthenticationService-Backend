package edu.dosw.infrastructure.web;

import edu.dosw.application.dto.UserCredentialsResponseDTO;
import edu.dosw.application.dto.UserInfoDTO;
import edu.dosw.application.ports.UserInfoUseCase;
import edu.dosw.domain.model.ValueObject.Email;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-info")
@RequiredArgsConstructor
@Tag(name = "User Info", description = "User information endpoints")
public class UserInfoController {

    private final UserInfoUseCase userInfoUseCase;

    @GetMapping("/credentials/{email}")
    @Operation(summary = "Get user credentials by email")
    public ResponseEntity<UserCredentialsResponseDTO> getUserCredentials(@PathVariable String email) {
        UserCredentialsResponseDTO credentials = userInfoUseCase.getUserCredentials(new Email(email));
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current user info")
    public ResponseEntity<UserInfoDTO> getCurrentUserInfo(@RequestParam String email) {
        UserInfoDTO userInfo = userInfoUseCase.getCurrentUserInfo(email);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/can-access-user/{targetUserId}")
    @Operation(summary = "Check if user can access user data")
    public ResponseEntity<Boolean> canAccessUserData(
            @RequestParam String currentUserEmail,
            @PathVariable String targetUserId) {
        boolean canAccess = userInfoUseCase.canAccessUserData(currentUserEmail, targetUserId);
        return ResponseEntity.ok(canAccess);
    }

    @GetMapping("/can-access-student/{studentId}")
    @Operation(summary = "Check if user can access student data")
    public ResponseEntity<Boolean> canAccessStudentData(
            @RequestParam String currentUserEmail,
            @PathVariable String studentId) {
        boolean canAccess = userInfoUseCase.canAccessStudentData(currentUserEmail, studentId);
        return ResponseEntity.ok(canAccess);
    }
}